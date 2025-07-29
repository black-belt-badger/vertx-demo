package bbb.vertx_demo.main.http_server;

import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.common.template.TemplateEngine;
import io.vertx.ext.web.healthchecks.HealthCheckHandler;
import io.vertx.pgclient.PgConnection;
import io.vertx.redis.client.RedisAPI;
import io.vertx.redis.client.RedisConnection;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;

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

@Slf4j
public enum NewsGeneral {

  ;

  private static final String REDIS_KEY = "/general-news";
  private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("EEEE, MMM d',' HH:mm", US);

  static Handler<RoutingContext> generalNews
    (
      String name,
      HealthCheckHandler checks,
      WebClient client,
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
      redisApi.get(REDIS_KEY)
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
              var query = "SELECT category, datetime, headline, image, related, source, summary, url FROM finnhub.news_general_view ORDER BY datetime DESC";
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
                    renderingContext.put("pageTitle", "General news");
                    renderingContext.put("tableHeader", "General news");
                    renderingContext.put("tableSubheader", "A broad look at the latest financial headlines shaping global markets. This feed highlights economic trends, corporate moves, and policy changes across sectors.");
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
                    engine
                      .render(renderingContext, "templates/general-news.html")
                      .onFailure(throwable ->
                        log.error("error rendering '{}' template", name, throwable)
                      )
                      .onSuccess(buffer -> {
                          context.response()
                            .putHeader(CONTENT_TYPE, HTML)
                            .putHeader(CACHE_CONTROL, cacheControl)
                            .end(buffer);
                          var request = cmd(SETEX)
                            .arg(REDIS_KEY)
                            .arg(maxAge)
                            .arg(buffer);
                          redisConnection.send(request);
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
