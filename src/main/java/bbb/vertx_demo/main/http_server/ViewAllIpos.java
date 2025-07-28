package bbb.vertx_demo.main.http_server;

import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.common.template.TemplateEngine;
import io.vertx.redis.client.RedisAPI;
import io.vertx.redis.client.RedisConnection;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.List;

import static bbb.vertx_demo.main.http_server.HttpServerStarter.*;
import static com.google.common.base.Stopwatch.createStarted;
import static com.google.common.net.MediaType.HTML_UTF_8;
import static io.vertx.core.http.HttpHeaders.CACHE_CONTROL;
import static io.vertx.core.http.HttpHeaders.CONTENT_TYPE;
import static io.vertx.redis.client.Command.SETEX;
import static io.vertx.redis.client.Request.cmd;
import static java.lang.String.format;
import static java.lang.System.getenv;
import static java.util.Objects.nonNull;
import static java.util.Optional.ofNullable;

@Slf4j
public enum ViewAllIpos {

  ;

  private static final String VERSION = ofNullable(getenv("VERSION")).orElse("unknown");
  private static final String FINNHUB_URL = "/api/v1/calendar/ipo";
  public static final String HTML = HTML_UTF_8.toString();

  private static final String REDIS_KEY = "/about";

  static Handler<RoutingContext> viewAllIpos
    (
      String name,
      WebClient webClient,
      TemplateEngine engine,
      RedisAPI redisApi,
      RedisConnection redisConnection,
      JsonObject config
    ) {
    return context -> {
      var maxAgeString = config.getString("max-age", "PT1S");
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
              webClient
                .get(FINNHUB_PORT, FINNHUB_HOST, FINNHUB_URL)
                .putHeader(FINNHUB_HEADER, FINNHUB_API_KEY)
                .send()
                .onFailure(throwable -> log.error("error sending '{}' request", name, throwable))
                .onSuccess(response -> {
                    var object = response.bodyAsJsonObject();
                    var ipoCalendar = object.getJsonArray("ipoCalendar");
                    var reneringContext =
                      new JsonObject()
                        .put("ipoCalendar", ipoCalendar);
                    JsonArray main = reneringContext.getJsonArray("ipoCalendar");
                  List list = main.getList();
                  engine
                      .render(reneringContext, "templates/view-all-ipos.html")
                      .onFailure(throwable -> log.error("error rendering '{}' template", name, throwable))
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
