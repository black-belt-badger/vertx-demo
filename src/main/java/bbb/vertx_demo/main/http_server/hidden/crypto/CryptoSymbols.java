package bbb.vertx_demo.main.http_server.hidden.crypto;

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

import static bbb.vertx_demo.main.http_server.Home.HTML;
import static bbb.vertx_demo.main.http_server.HttpServerStarter.*;
import static com.google.common.base.Stopwatch.createStarted;
import static io.vertx.core.http.HttpHeaders.CACHE_CONTROL;
import static io.vertx.core.http.HttpHeaders.CONTENT_TYPE;
import static io.vertx.redis.client.Command.SETEX;
import static io.vertx.redis.client.Request.cmd;
import static java.lang.String.format;
import static java.util.Comparator.comparing;
import static java.util.Objects.nonNull;

@Slf4j
public enum CryptoSymbols {

  ;

  public static Handler<RoutingContext> cryptoSymbol
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
      log.info("Cache expiry for crypto symbols is {} seconds", maxAge);
      var cacheControl = format("public, max-age=%d, immutable", maxAge);
      var watch = createStarted();
      var exchange = context.pathParam("exchange");
      var redisKey = "/api/v1/crypto/symbol?exchange=" + exchange;
      redisApi
        .get(redisKey)
        .onFailure(throwable -> log.error("error getting crypto symbols from Redis", throwable))
        .onSuccess(redisResponse -> {
            if (nonNull(redisResponse)) {
              var buffer = redisResponse.toBuffer();
              context.response()
                .putHeader(CONTENT_TYPE, HTML)
                .putHeader(CACHE_CONTROL, cacheControl)
                .end(buffer);
              log.info("Crypto symbols request handled in {}", watch.elapsed());
            } else {
              var finnhubUrl = "/api/v1/crypto/symbol?exchange=" + exchange;
              webClient
                .get(FINNHUB_PORT, FINNHUB_HOST, finnhubUrl)
                .putHeader(FINNHUB_HEADER, FINNHUB_API_KEY)
                .send()
                .onFailure(throwable -> log.error("error sending request", throwable))
                .onSuccess(response -> {
                    var array = response.bodyAsJsonArray();
                    var list =
                      array.stream()
                        .map(o -> (JsonObject) o)
                        .sorted(comparing(obj -> obj.getString("displaySymbol")))
                        .toList();
                    var map =
                      new JsonArray(list).stream().map(o ->
                          ((JsonObject) o).getMap()
                        )
                        .toList();
                    engine
                      .render(new JsonObject().put("symbols", map), "templates/crypto/symbol.html")
                      .onFailure(throwable -> log.error("error rendering crypto symbols template", throwable))
                      .onSuccess(buffer -> {
                          context.response()
                            .putHeader(CONTENT_TYPE, HTML)
                            .putHeader(CACHE_CONTROL, cacheControl)
                            .end(buffer);
                          log.info("Crypto symbols request handled in {}", watch.elapsed());
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
