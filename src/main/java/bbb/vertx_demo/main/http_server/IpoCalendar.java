package bbb.vertx_demo.main.http_server;

import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.common.template.TemplateEngine;
import io.vertx.redis.client.RedisAPI;
import io.vertx.redis.client.RedisConnection;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;

import static bbb.vertx_demo.main.http_server.Handlers.HTML;
import static bbb.vertx_demo.main.http_server.HttpServerStarter.*;
import static com.google.common.base.Stopwatch.createStarted;
import static io.vertx.core.http.HttpHeaders.CACHE_CONTROL;
import static io.vertx.core.http.HttpHeaders.CONTENT_TYPE;
import static io.vertx.redis.client.Command.SETEX;
import static io.vertx.redis.client.Request.cmd;
import static java.lang.String.format;
import static java.util.Objects.nonNull;

@Slf4j
public enum IpoCalendar {

  ;

  private static final String FINNHUB_URL = "/api/v1/calendar/ipo";
  private static final String REDIS_KEY = "/api/v1/calendar/ipo";
  private static final String TEMPLATE_KEY = "object";

  static Handler<RoutingContext> ipoCalendar
    (
      WebClient webClient,
      TemplateEngine engine,
      RedisAPI redisApi,
      RedisConnection redisConnection,
      JsonObject config
    ) {
    return context -> {
      var maxAgeString = config.getString("max-age", "PT1H");
      var maxAge = Duration.parse(maxAgeString).toSeconds();
      log.info("Cache expiry for ipo calendar is {} seconds", maxAge);
      var cacheControl = format("public, max-age=%d, immutable", maxAge);
      var watch = createStarted();
      redisApi
        .get(REDIS_KEY)
        .onFailure(throwable -> log.error("error getting ipo calendar from Redis", throwable))
        .onSuccess(redisResponse -> {
            if (nonNull(redisResponse)) {
              var buffer = redisResponse.toBuffer();
              context.response()
                .putHeader(CONTENT_TYPE, HTML)
                .putHeader(CACHE_CONTROL, cacheControl)
                .end(buffer);
              log.info("FDA calendar request handled in {}", watch.elapsed());
            } else {
              webClient
                .get(FINNHUB_PORT, FINNHUB_HOST, FINNHUB_URL)
                .putHeader(FINNHUB_HEADER, FINNHUB_API_KEY)
                .send()
                .onFailure(throwable -> log.error("error sending ipo calendar request", throwable))
                .onSuccess(response -> {
                    var object = response.bodyAsJsonObject();
                    engine
                      .render(new JsonObject().put(TEMPLATE_KEY, object), "templates/calendar-ipo.html")
                      .onFailure(throwable -> log.error("error rendering ipo calendar template", throwable))
                      .onSuccess(buffer -> {
                          context.response()
                            .putHeader(CONTENT_TYPE, HTML)
                            .putHeader(CACHE_CONTROL, cacheControl)
                            .end(buffer);
                          log.info("IPO calendar request handled in {}", watch.elapsed());
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
