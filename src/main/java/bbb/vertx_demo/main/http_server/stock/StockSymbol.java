package bbb.vertx_demo.main.http_server.stock;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.ReplyException;
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
import static io.vertx.core.Future.succeededFuture;
import static io.vertx.core.ThreadingModel.WORKER;
import static io.vertx.core.http.HttpHeaders.CACHE_CONTROL;
import static io.vertx.core.http.HttpHeaders.CONTENT_TYPE;
import static io.vertx.redis.client.Command.SETEX;
import static io.vertx.redis.client.Request.cmd;
import static java.lang.String.format;
import static java.util.Comparator.comparing;
import static java.util.Objects.nonNull;

@Slf4j
public enum StockSymbol {

  ;

  // TODO: Stock Exchanges page
  // TODO: paging
  // TODO: filtering by type
  // TODO: filtering by currency
  // TODO: sorting by type and currency
  // TODO: summary information: symbol count column
  // TODO: market status column

  private static final String SYMBOLS_ADDRESS = "symbols";
  private static final String FINNHUB_URL_PREFIX = "/api/v1/stock/symbol?exchange=";
  private static final String REDIS_KEY_PREFIX = "/api/v1/stock/symbol?exchange=";

  public static void deploySymbolVerticle(Vertx vertx, WebClient client) {
    vertx
      .deployVerticle(ctx -> {
          vertx.eventBus()
            .consumer(SYMBOLS_ADDRESS)
            .handler(busMessage -> {
                var body = busMessage.body();
                var exchange = (String) body;
                var watch = createStarted();
                client
                  .get(FINNHUB_PORT, FINNHUB_HOST, FINNHUB_URL_PREFIX + exchange)
                  .putHeader(FINNHUB_HEADER, FINNHUB_API_KEY)
                  .send()
                  .onFailure(throwable -> {
                      log.error("Error getting finnhub stock symbol in {}", watch.elapsed(), throwable);
                      busMessage.fail(-1, throwable.getMessage());
                    }
                  )
                  .onSuccess(response -> {
                      log.info("Symbol request retrieving succeeded in {}", watch.elapsed());
                      var buffer = response.body();
                      busMessage.reply(buffer);
                    }
                  );
              }
            );
          return succeededFuture();
        },
        new DeploymentOptions().setThreadingModel(WORKER)
      )
      .onFailure(throwable -> log.error("Error deploying symbol verticle", throwable))
      .onSuccess(response -> log.info("Deployed symbol verticle with {}", response));
  }

  public static Handler<RoutingContext> stockSymbol
    (
      Vertx vertx,
      TemplateEngine engine,
      RedisAPI redisApi,
      RedisConnection redisConnection,
      JsonObject config
    ) {
    return context -> {
      var exchange = context.pathParam("exchange");
      var maxAgeString = config.getString("max-age", "PT1H");
      var maxAge = Duration.parse(maxAgeString).toSeconds();
      log.info("Cache expiry for forex exchanges is {} seconds", maxAge);
      var cacheControl = format("public, max-age=%d, immutable", maxAge);
      var watch = createStarted();
      redisApi
        .get(REDIS_KEY_PREFIX + exchange)
        .onFailure(throwable -> log.error("error getting forex exchanges from Redis", throwable))
        .onSuccess(redisResponse -> {
            if (nonNull(redisResponse)) {
              var buffer = redisResponse.toBuffer();
              context.response()
                .putHeader(CONTENT_TYPE, HTML)
                .putHeader(CACHE_CONTROL, cacheControl)
                .end(buffer);
              log.info("Forex exchanges request handled in {}", watch.elapsed());
            } else {
              vertx.eventBus()
                .request(SYMBOLS_ADDRESS, exchange)
                .onFailure(eventBusError -> {
                    log.error("failed requesting symbols on event bus", eventBusError);
                    if (eventBusError instanceof ReplyException exception) {
                      engine
                        .render(new JsonObject().put("error", exception.getMessage()), "/templates/common/error.html")
                        .onFailure(renderingError -> log.error("error rendering template", renderingError))
                        .onSuccess(buffer ->
                          context.response()
                            .setStatusCode(500)
                            .putHeader(CONTENT_TYPE, HTML).end(buffer)
                        );
                    }
                  }
                )
                .onSuccess(response -> {
                    var string = response.body().toString();
                    var array = new JsonArray(string);
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
                      .render(new JsonObject().put("symbols", map), "templates/stock/symbol.html")
                      .onFailure(renderingError -> log.error("error rendering error template", renderingError))
                      .onSuccess(buffer -> {
                          context.response()
                            .putHeader(CONTENT_TYPE, HTML)
                            .putHeader(CACHE_CONTROL, cacheControl)
                            .end(buffer);
                          log.info("Forex exchanges request handled in {}", watch.elapsed());
                          var request = cmd(SETEX)
                            .arg(REDIS_KEY_PREFIX + exchange)
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
      ;
    };
  }
}
