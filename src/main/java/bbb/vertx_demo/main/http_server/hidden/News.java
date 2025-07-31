package bbb.vertx_demo.main.http_server.hidden;

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
public enum News {

  ;

  private static final String REDIS_KEY_PREFIX = "/api/v1/news?category=";

  public static Handler<RoutingContext> news
    (
      WebClient client,
      TemplateEngine engine,
      RedisAPI redisApi,
      RedisConnection redisConnection,
      JsonObject config
    ) {
    return context -> {
      var category = context.pathParam("category");
      var maxAgeString = config.getString("max-age", "PT1S");
      var maxAge = Duration.parse(maxAgeString).toSeconds();
      log.info("Cache expiry for {} news is {} seconds", category, maxAge);
      var cacheControl = format("public, max-age=%d, immutable", maxAge);
      var watch = createStarted();
      var redisKey = REDIS_KEY_PREFIX + category;
      redisApi.get(redisKey)
        .onFailure(throwable -> log.error("error getting {} news from Redis", category, throwable))
        .onSuccess(redisResponse -> {
            if (nonNull(redisResponse)) {
              var buffer = redisResponse.toBuffer();
              context.response()
                .putHeader(CONTENT_TYPE, HTML)
                .putHeader(CACHE_CONTROL, cacheControl)
                .end(buffer);
              log.info("{} news request handled in {}", category, watch.elapsed());
            } else {
              client
                .get(FINNHUB_PORT, FINNHUB_HOST, "/api/v1/news?category=" + category)
                .putHeader(FINNHUB_HEADER, FINNHUB_API_KEY)
                .send()
                .onFailure(throwable -> log.error("error {} news template", category, throwable))
                .onSuccess(response -> {
                    var array = response.bodyAsJsonArray();
                    var renderingContext = new JsonObject().put("news", array).put("caption", category + " news");
                    engine
                      .render(renderingContext, "templates/news.html")
                      .onFailure(throwable -> log.error("error {} news rendering template", category, throwable))
                      .onSuccess(buffer -> {
                          context.response()
                            .putHeader(CONTENT_TYPE, HTML)
                            .putHeader(CACHE_CONTROL, cacheControl)
                            .end(buffer);
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
