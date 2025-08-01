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
import io.vertx.redis.client.Response;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.HashMap;

import static bbb.vertx_demo.main.db.IpoUpdater.IPOS_EPOCH_MILLIS;
import static bbb.vertx_demo.main.db.NewsUpdater.EPOCH_MILLIS;
import static bbb.vertx_demo.main.http_server.FormattingHelper.*;
import static com.google.common.base.Stopwatch.createStarted;
import static com.google.common.net.MediaType.HTML_UTF_8;
import static io.vertx.core.http.HttpHeaders.CACHE_CONTROL;
import static io.vertx.core.http.HttpHeaders.CONTENT_TYPE;
import static io.vertx.ext.healthchecks.Status.KO;
import static io.vertx.redis.client.Command.SETEX;
import static io.vertx.redis.client.Request.cmd;
import static java.lang.String.format;
import static java.time.ZoneId.systemDefault;
import static java.util.Objects.nonNull;
import static java.util.Optional.ofNullable;

@Slf4j
public enum Ipos {

  ;

  public static final String HTML = HTML_UTF_8.toString();

  public static final String REDIS_KEY = "/ipos";

  static Handler<RoutingContext> viewAllIpos
    (
      String name,
      HealthCheckHandler checks,
      WebClient webClient,
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
      redisApi
        .get(REDIS_KEY)
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
              var query = "SELECT date, exchange, name, number_of_shares, price, status, symbol, total_shares_value, price_number, price_from, price_to FROM finnhub.calendar_ipo_parsed ORDER BY date DESC, name";
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
                    renderingContext.put("pageTitle", "IPOs");
                    var elements = rowSet.stream().map(row -> {
                        var element = new HashMap<String, Object>();
                        element.put("date", row.getLocalDate("date"));
                        element.put("name", row.getString("name"));
                        element.put("exchange", row.getString("exchange"));
                        var numberOfShares = row.getLong("number_of_shares");
                        var numberOfSharesFormatted = longWithCommas(numberOfShares);
                        element.put("number_of_shares", numberOfSharesFormatted);
                        var price = row.getString("price");
                        element.put("price", price);
                        String status;
                        if (price == null || price.isBlank()) {
                          status = "unknown";
                        } else if (price.contains("-")) {
                          status = "range";
                        } else {
                          status = "single";
                        }
                        element.put("status", status);
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
                        String priceFormatted;
                        if (price == null || price.isBlank())
                          priceFormatted = "N/A";
                        else if (status.equals("single"))
                          priceFormatted = priceNumberFormatted;
                        else
                          priceFormatted = priceFromFormatted + " - " + priceToFormatted;
                        element.put("price_formatted", priceFormatted);
                        var totalSharesValue = row.getLong("total_shares_value");
                        var totalSharesValueFormatted = longWithCommas(totalSharesValue);
                        element.put("total_shares_value", totalSharesValueFormatted);
                        return element;
                      }
                    ).toList();
                    renderingContext.put("elements", elements);
                    redisApi
                      .hget(EPOCH_MILLIS, IPOS_EPOCH_MILLIS)
                      .onFailure(throwable -> log.error("error getting last updated", throwable))
                      .onSuccess(response -> {
                          var epochMillis = ofNullable(response).map(Response::toString).orElse("unknown");
                          renderingContext.put("epochMillis", epochMillis);
                          var lastNews = rowSet.iterator().next();
                          var lastNewsDate = lastNews.getLocalDate("date");
                          var lastNewsDateTime = lastNewsDate.atStartOfDay();
                          long lastNewsEpochMilli = lastNewsDateTime.atZone(systemDefault()).toInstant().toEpochMilli();
                          renderingContext.put("lastNewsEpochMilli", lastNewsEpochMilli);
                          engine
                            .render(renderingContext, "templates/ipos.html")
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
                                redisConnection
                                  .send(request)
                                  .onFailure(throwable -> log.error("Error setting {} max age in Redis", name, throwable))
                                  .onSuccess(ack -> log.info("Set {} max age in Redis {}", name, ack));
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
