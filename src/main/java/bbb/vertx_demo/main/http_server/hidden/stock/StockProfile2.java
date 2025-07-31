package bbb.vertx_demo.main.http_server.hidden.stock;

import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.common.template.TemplateEngine;
import io.vertx.redis.client.RedisAPI;
import io.vertx.redis.client.RedisConnection;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;

import static bbb.vertx_demo.main.http_server.Home.HTML;
import static bbb.vertx_demo.main.http_server.HttpServerStarter.*;
import static com.google.common.base.Stopwatch.createStarted;
import static io.vertx.core.http.HttpHeaders.CACHE_CONTROL;
import static io.vertx.core.http.HttpHeaders.CONTENT_TYPE;
import static io.vertx.redis.client.Command.SETEX;
import static io.vertx.redis.client.Request.cmd;
import static java.lang.String.format;
import static java.util.Objects.nonNull;

@Slf4j
public enum StockProfile2 {

  ;

  private static final String FINNHUB_URL_PREFIX = "/api/v1/stock/profile2?symbol=";
  private static final String REDIS_KEY_PREFIX = "/api/v1/stock/profile2?symbol=";

  public static Handler<RoutingContext> stockProfile2
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
      log.info("Cache expiry for stock profile 2 is {} seconds", maxAge);
      var cacheControl = format("public, max-age=%d, immutable", maxAge);
      var watch = createStarted();
      var symbol = context.pathParam("symbol");
      var redisKey = REDIS_KEY_PREFIX + symbol;
      redisApi
        .get(redisKey)
        .onFailure(throwable -> log.error("error getting stock profile 2 from Redis", throwable))
        .onSuccess(redisResponse -> {
            if (nonNull(redisResponse)) {
              var buffer = redisResponse.toBuffer();
              context.response()
                .putHeader(CONTENT_TYPE, HTML)
                .putHeader(CACHE_CONTROL, cacheControl)
                .end(buffer);
              log.info("Stock profile 2 request handled in {}", watch.elapsed());
            } else {
              var finnhubUrl = FINNHUB_URL_PREFIX + symbol;
              webClient
                .get(FINNHUB_PORT, FINNHUB_HOST, finnhubUrl)
                .putHeader(FINNHUB_HEADER, FINNHUB_API_KEY)
                .send()
                .onFailure(throwable -> log.error("error sending stock profile 2 request", throwable))
                .onSuccess(response -> {
                    var object = response.bodyAsJsonObject();
                    engine
                      .render(new JsonObject().put("profile", object), "templates/stock/profile2.html")
                      .onFailure(throwable -> log.error("error rendering stock profile 2 template", throwable))
                      .onSuccess(buffer -> {
                          context.response()
                            .putHeader(CONTENT_TYPE, HTML)
                            .putHeader(CACHE_CONTROL, cacheControl)
                            .end(buffer);
                          log.info("Stock profile 2 request handled in {}", watch.elapsed());
                          var request = cmd(SETEX)
                            .arg(redisKey)
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
