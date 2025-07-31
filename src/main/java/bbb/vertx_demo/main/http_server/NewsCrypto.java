package bbb.vertx_demo.main.http_server;

import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.common.template.TemplateEngine;
import io.vertx.ext.web.healthchecks.HealthCheckHandler;
import io.vertx.pgclient.PgConnection;
import io.vertx.redis.client.RedisAPI;
import io.vertx.redis.client.RedisConnection;
import io.vertx.redis.client.Response;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;

import static bbb.vertx_demo.main.NewsCategory.CRYPTO;
import static bbb.vertx_demo.main.db.NewsUpdater.EPOCH_MILLIS;
import static bbb.vertx_demo.main.http_server.Home.HTML;
import static com.google.common.base.Stopwatch.createStarted;
import static io.vertx.core.http.HttpHeaders.CACHE_CONTROL;
import static io.vertx.core.http.HttpHeaders.CONTENT_TYPE;
import static io.vertx.ext.healthchecks.Status.KO;
import static io.vertx.redis.client.Command.SETEX;
import static io.vertx.redis.client.Request.cmd;
import static java.lang.String.format;
import static java.util.Locale.US;
import static java.util.Objects.nonNull;
import static java.util.Optional.ofNullable;

@Slf4j
public enum NewsCrypto {

  ;

  private static final String CRYPTO_NEWS_CACHED_CONTENT = "/crypto-news";
  private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("EEEE, MMM d',' HH:mm", US);

  static Handler<RoutingContext> cryptoNews
    (
      String name,
      HealthCheckHandler checks,
      TemplateEngine engine,
      RedisAPI redisApi,
      RedisConnection redisConnection,
      PgConnection pgConnection,
      JsonObject config
    ) {
    return context -> {
      var maxAgeString = config.getString("max-age", "PT1H");
      var maxAge = Duration.parse(maxAgeString).toSeconds();
      log.info("Cache expiry for '{}' is {} seconds", name, maxAge);
      var cacheControl = format("public, max-age=%d, immutable", maxAge);
      var watch = createStarted();
      redisApi.get(CRYPTO_NEWS_CACHED_CONTENT)
        .onFailure(throwable -> log.error("error getting '{}' from Redis", name, throwable))
        .onSuccess(redisResponse -> {
            if (nonNull(redisResponse)) {
              var buffer = redisResponse.toBuffer();
              context.response()
                .putHeader(CONTENT_TYPE, HTML)
                .putHeader(CACHE_CONTROL, cacheControl)
                .end(buffer);
              log.info("'{}' request handled in {}", name, watch.elapsed());
            } else {
              var query = "SELECT category, datetime, headline, image, related, source, summary, url FROM finnhub.news_crypto_view ORDER BY datetime DESC";
              pgConnection
                .query(query)
                .execute()
                .onFailure(throwable -> {
                    checks.register(query, promise ->
                      promise.complete(KO(), throwable)
                    );
                    log.error("Query {} failed", query, throwable);
                  }
                )
                .onSuccess(rowSet -> {
                    var renderingContext = new HashMap<String, Object>();
                    renderingContext.put("pageTitle", "Crypto news");
                    renderingContext.put("tableHeader", "Crypto news");
                    renderingContext.put("tableSubheader", "Insights from the fast-moving world of cryptocurrencies and digital assets. Track regulation updates, market volatility, and emerging blockchain trends.");
                    renderingContext.put("category", CRYPTO.value);
                    var elements = rowSet.stream().map(row -> {
                        var element = new HashMap<String, Object>();
                        var datetime = row.getLocalDateTime("datetime");
                        var formatted = datetime.format(DATE_TIME_FORMATTER);
                        element.put("datetime", formatted);
                        element.put("headline", row.getString("headline"));
                        element.put("summary", row.getString("summary"));
                        element.put("url", row.getString("url"));
                        return element;
                      }
                    ).toList();
                    renderingContext.put("elements", elements);
                    redisApi
                      .hget(EPOCH_MILLIS, CRYPTO.value)
                      .onFailure(throwable -> log.error("error getting last updated", throwable))
                      .onSuccess(response -> {
                          var epochMillis = ofNullable(response).map(Response::toString).orElse("unknown");
                          renderingContext.put("epochMillis", epochMillis);
                          var lastNews = rowSet.iterator().next();
                          var lastNewsDateTime = lastNews.getLocalDateTime("datetime");
                          long lastNewsEpochMilli = lastNewsDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
                          renderingContext.put("lastNewsEpochMilli", lastNewsEpochMilli);
                          engine
                            .render(renderingContext, "templates/news.html")
                            .onFailure(throwable -> log.error("error rendering '{}' template", name, throwable))
                            .onSuccess(buffer -> {
                                context.response()
                                  .putHeader(CONTENT_TYPE, HTML)
                                  .putHeader(CACHE_CONTROL, cacheControl)
                                  .end(buffer);
                                var request = cmd(SETEX)
                                  .arg(CRYPTO_NEWS_CACHED_CONTENT)
                                  .arg(maxAge)
                                  .arg(buffer);
                                redisConnection.send(request);
                              }
                            );
                        }
                      );
                  }
                );
            }
          }
        );
    };
  }
}
