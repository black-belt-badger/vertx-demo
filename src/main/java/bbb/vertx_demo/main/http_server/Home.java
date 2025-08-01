package bbb.vertx_demo.main.http_server;

import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.common.template.TemplateEngine;
import io.vertx.ext.web.healthchecks.HealthCheckHandler;
import io.vertx.pgclient.PgConnection;
import io.vertx.redis.client.RedisAPI;
import io.vertx.redis.client.RedisConnection;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.HashMap;

import static bbb.vertx_demo.main.http_server.FormattingHelper.*;
import static com.google.common.base.Stopwatch.createStarted;
import static com.google.common.net.MediaType.HTML_UTF_8;
import static io.vertx.core.http.HttpHeaders.CACHE_CONTROL;
import static io.vertx.core.http.HttpHeaders.CONTENT_TYPE;
import static io.vertx.ext.healthchecks.Status.KO;
import static io.vertx.redis.client.Command.SETEX;
import static io.vertx.redis.client.Request.cmd;
import static java.lang.String.format;
import static java.lang.System.getenv;
import static java.util.Objects.nonNull;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.groupingBy;

@Slf4j
public enum Home {

  ;

  private static final String VERSION = ofNullable(getenv("VERSION")).orElse("unknown");
  public static final String HTML = HTML_UTF_8.toString();

  private static final String REDIS_KEY = "/";

  static Handler<RoutingContext> home
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
      log.info("Cache expiry for home page is {} seconds", maxAge);
      var cacheControl = format("public, max-age=%d, immutable", maxAge);
      var watch = createStarted();
      redisApi
        .get(REDIS_KEY)
        .onFailure(throwable -> log.error("error getting home page from Redis", throwable))
        .onSuccess(redisResponse -> {
            if (nonNull(redisResponse)) {
              var buffer = redisResponse.toBuffer();
              context.response()
                .putHeader(CONTENT_TYPE, HTML)
                .putHeader(CACHE_CONTROL, cacheControl)
                .end(buffer);
              log.info("Home page request handled in {}", watch.elapsed());
            } else {
              var iposQuery = """
                SELECT *
                              FROM finnhub.calendar_ipo_parsed
                              WHERE date >= CURRENT_DATE
                              ORDER BY date
                """;
              pgConnection
                .query(iposQuery)
                .execute()
                .onFailure(throwable -> {
                    checks.register(iposQuery, promise ->
                      promise.complete(KO(), throwable)
                    );
                    log.error("Query {} failed", iposQuery, throwable);
                  }
                )
                .onSuccess(iposRowSet -> {
                    var renderingContext = new HashMap<String, Object>();
                    renderingContext.put("pageTitle", "Home page");
                    var ipoElements = iposRowSet.stream().map(row -> {
                        var element = new HashMap<String, Object>();
                        element.put("date", row.getLocalDate("date"));
                        element.put("name", row.getString("name"));
                        element.put("exchange", String.valueOf(row.getString("exchange")));
                        var numberOfShares = row.getLong("number_of_shares");
                        var numberOfSharesFormatted = longCompact(numberOfShares);
                        element.put("number_of_shares", numberOfSharesFormatted);
                        String price = row.getString("price");
                        element.put("price", price);
                        var priceNumber = row.getBigDecimal("price_number");
                        var priceNumberFormatted = bigDecimalCompactPrice(priceNumber);
                        element.put("price_number", priceNumberFormatted);
                        var priceFrom = row.getBigDecimal("price_from");
                        var priceFromFormatted = bigDecimalCompactPrice(priceFrom);
                        element.put("price_from", priceFromFormatted);
                        var priceTo = row.getBigDecimal("price_to");
                        var priceToFormatted = bigDecimalCompactPrice(priceTo);
                        element.put("price_to", priceToFormatted);
                        element.put("status", row.getString("status"));
                        String priceFormat;
                        if (price == null || price.isBlank()) {
                          priceFormat = "unknown";
                        } else if (price.contains("-")) {
                          priceFormat = "range";
                        } else {
                          priceFormat = "single";
                        }
                        String priceFormatted;
                        if (price == null || price.isBlank())
                          priceFormatted = "N/A";
                        else if (priceFormat.equals("single"))
                          priceFormatted = priceNumberFormatted;
                        else
                          priceFormatted = priceFromFormatted + " - " + priceToFormatted;
                        element.put("price_formatted", priceFormatted);
                        var totalSharesValue = row.getLong("total_shares_value");
                      var totalSharesValueFormatted = longCompact(totalSharesValue);
                        element.put("total_shares_value", totalSharesValueFormatted);
                        return element;
                      }
                    ).toList();
                    var elementsByExchange =
                      ipoElements.stream().collect(groupingBy(map ->
                        map.getOrDefault("exchange", "null").toString())
                      );
                    renderingContext.put("elementsByExchange", elementsByExchange);
                    var newsQuery = """
                      SELECT TO_CHAR(datetime, 'HH24:MI') AS time, headline, summary, url
                         FROM finnhub.news_general_view
                         WHERE datetime >= CURRENT_DATE
                         UNION ALL
                         SELECT TO_CHAR(datetime, 'HH24:MI') AS time, headline, summary, url
                         FROM finnhub.news_crypto_view
                         WHERE datetime >= CURRENT_DATE
                         UNION ALL
                         SELECT TO_CHAR(datetime, 'HH24:MI') AS time, headline, summary, url
                         FROM finnhub.news_merger_view
                         WHERE datetime >= CURRENT_DATE
                         ORDER BY time DESC
                      """;
                    pgConnection
                      .query(newsQuery)
                      .execute()
                      .onFailure(throwable -> {
                          checks.register(newsQuery, promise ->
                            promise.complete(KO(), throwable)
                          );
                          log.error("Query {} failed", newsQuery, throwable);
                        }
                      )
                      .onSuccess(newsRowSet -> {
                          var newsElements = newsRowSet.stream().map(row -> {
                              var element = new HashMap<String, Object>();
                              element.put("time", row.getString("time"));
                              element.put("headline", String.valueOf(row.getString("headline")));
                              element.put("summary", String.valueOf(row.getString("summary")));
                              element.put("url", String.valueOf(row.getString("url")));
                              return element;
                            }
                          ).toList();
                          renderingContext.put("news", newsElements);
                          engine
                            .render(renderingContext, "templates/home.html")
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
                );
            }
          }
        );
    };
  }
}
