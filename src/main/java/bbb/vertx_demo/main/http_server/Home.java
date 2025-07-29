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
              var query = """
                SELECT *
                FROM (
                       SELECT *,
                              ROW_NUMBER() OVER (PARTITION BY exchange ORDER BY date DESC) AS rn
                       FROM finnhub.calendar_ipo_parsed
                     ) sub
                WHERE rn <= 3;""";
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
                    renderingContext.put("pageTitle", "Home page");
                    var elements = rowSet.stream().map(row -> {
                        var element = new HashMap<String, Object>();
                        element.put("date", row.getLocalDate("date"));
                        element.put("name", row.getString("name"));
                        element.put("exchange", String.valueOf(row.getString("exchange")));
                        element.put("number_of_shares", row.getInteger("number_of_shares"));
                        element.put("price", row.getString("price"));
                        element.put("price_number", row.getBigDecimal("price_number"));
                        element.put("price_from", row.getBigDecimal("price_from"));
                        element.put("price_to", row.getBigDecimal("price_to"));
                        element.put("status", row.getString("status"));
                        element.put("total_shares_value", row.getLong("total_shares_value"));
                        return element;
                      }
                    ).toList();
                    var elementsByExchange =
                      elements.stream().collect(groupingBy(map ->
                        map.getOrDefault("exchange", "null").toString())
                      );
                    renderingContext.put("elementsByExchange", elementsByExchange);
                    engine
                      .render(renderingContext, "templates/index.html")
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
