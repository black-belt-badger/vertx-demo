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
import io.vertx.ext.web.templ.thymeleaf.ThymeleafTemplateEngine;
import lombok.extern.slf4j.Slf4j;

import static bbb.vertx_demo.main.http_server.HttpServerStarter.*;
import static com.google.common.base.Stopwatch.createStarted;
import static io.vertx.core.Future.succeededFuture;
import static io.vertx.core.ThreadingModel.WORKER;

@Slf4j
public enum StockHandlers {

  ;

  public static Handler<RoutingContext> stockEarnings(WebClient client, ThymeleafTemplateEngine engine) {
    return context -> {
      var symbol = context.pathParam("symbol");
      client
        .get(FINNHUB_PORT, FINNHUB_HOST, "/api/v1/stock/earnings?symbol=" + symbol)
        .putHeader(FINNHUB_HEADER, FINNHUB_API_KEY)
        .send()
        .onFailure(throwable -> log.error("error sending request", throwable))
        .onSuccess(response -> {
            var array = response.bodyAsJsonArray();
            engine
              .render(new JsonObject().put("array", array).put("symbol", symbol), "templates/stock/earnings.html")
              .onFailure(throwable -> log.error("error rendering template", throwable))
              .onSuccess(buffer ->
                context.response().putHeader("content-type", "text/html").end(buffer)
              );
          }
        );
    };
  }

  public static Handler<RoutingContext> stockFilings(WebClient client, ThymeleafTemplateEngine engine) {
    return context -> {
      var symbol = context.pathParam("symbol");
      client
        .get(FINNHUB_PORT, FINNHUB_HOST, "/api/v1/stock/filings?symbol=" + symbol)
        .putHeader(FINNHUB_HEADER, FINNHUB_API_KEY)
        .send()
        .onFailure(throwable -> log.error("error sending request", throwable))
        .onSuccess(response -> {
            var array = response.bodyAsJsonArray();
            engine
              .render(new JsonObject().put("array", array).put("symbol", symbol), "templates/stock/filings.html")
              .onFailure(throwable -> log.error("error rendering template", throwable))
              .onSuccess(buffer ->
                context.response().putHeader("content-type", "text/html").end(buffer)
              );
          }
        );
    };
  }

  public static Handler<RoutingContext> stockFinancialsReported(WebClient client, ThymeleafTemplateEngine engine) {
    return context -> {
      var symbol = context.pathParam("symbol");
      client
        .get(FINNHUB_PORT, FINNHUB_HOST, "/api/v1/stock/financials-reported?symbol=" + symbol)
        .putHeader(FINNHUB_HEADER, FINNHUB_API_KEY)
        .send()
        .onFailure(throwable -> log.error("error sending request", throwable))
        .onSuccess(response -> {
            var object = response.bodyAsJsonObject();
            engine
              .render(new JsonObject().put("object", object).put("symbol", symbol), "templates/stock/financials-reported.html")
              .onFailure(throwable -> log.error("error rendering template", throwable))
              .onSuccess(buffer ->
                context.response().putHeader("content-type", "text/html").end(buffer)
              );
          }
        );
    };
  }

  public static Handler<RoutingContext> stockInsiderTransactions(WebClient client, ThymeleafTemplateEngine engine) {
    return context -> {
      var symbol = context.pathParam("symbol");
      client
        .get(FINNHUB_PORT, FINNHUB_HOST, "/api/v1/stock/insider-transactions?symbol=" + symbol)
        .putHeader(FINNHUB_HEADER, FINNHUB_API_KEY)
        .send()
        .onFailure(throwable -> log.error("error sending request", throwable))
        .onSuccess(response -> {
            var object = response.bodyAsJsonObject();
            engine
              .render(new JsonObject().put("object", object).put("symbol", symbol), "templates/stock/insider-transactions.html")
              .onFailure(throwable -> log.error("error rendering template", throwable))
              .onSuccess(buffer ->
                context.response().putHeader("content-type", "text/html").end(buffer)
              );
          }
        );
    };
  }

  public static Handler<RoutingContext> stockInsiderSentiment(WebClient client, ThymeleafTemplateEngine engine) {
    return context -> {
      var symbol = context.pathParam("symbol");
      client
        .get(FINNHUB_PORT, FINNHUB_HOST, "/api/v1/stock/insider-sentiment?symbol=" + symbol)
        .putHeader(FINNHUB_HEADER, FINNHUB_API_KEY)
        .send()
        .onFailure(throwable -> log.error("error sending request", throwable))
        .onSuccess(response -> {
            var object = response.bodyAsJsonObject();
            engine
              .render(new JsonObject().put("object", object).put("symbol", symbol), "templates/stock/insider-sentiment.html")
              .onFailure(throwable -> log.error("error rendering template", throwable))
              .onSuccess(buffer ->
                context.response().putHeader("content-type", "text/html").end(buffer)
              );
          }
        );
    };
  }

  public static Handler<RoutingContext> stockMarketHoliday(WebClient client, ThymeleafTemplateEngine engine) {
    return context -> {
      var exchange = context.pathParam("exchange");
      client
        .get(FINNHUB_PORT, FINNHUB_HOST, "/api/v1/stock/market-holiday?exchange=" + exchange)
        .putHeader(FINNHUB_HEADER, FINNHUB_API_KEY)
        .send()
        .onFailure(throwable -> log.error("error sending request", throwable))
        .onSuccess(response -> {
            var object = response.bodyAsJsonObject();
            engine
              .render(new JsonObject().put("object", object).put("exchange", exchange), "templates/stock/market-holiday.html")
              .onFailure(throwable -> log.error("error rendering template", throwable))
              .onSuccess(buffer ->
                context.response().putHeader("content-type", "text/html").end(buffer)
              );
          }
        );
    };
  }

  public static Handler<RoutingContext> stockProfile2(WebClient client, TemplateEngine engine) {
    return context -> {
      var symbol = context.pathParam("symbol");
      client
        .get(FINNHUB_PORT, FINNHUB_HOST, "/api/v1/stock/profile2?symbol=" + symbol)
        .putHeader(FINNHUB_HEADER, FINNHUB_API_KEY)
        .send()
        .onFailure(throwable -> log.error("error sending request", throwable))
        .onSuccess(response -> {
            var object = response.bodyAsJsonObject();
            engine
              .render(new JsonObject().put("profile", object), "templates/stock/profile2.html")
              .onFailure(throwable -> log.error("error rendering template", throwable))
              .onSuccess(buffer ->
                context.response().putHeader("content-type", "text/html").end(buffer)
              );
          }
        );
    };
  }

  public static Handler<RoutingContext> stockRecommendation(WebClient client, ThymeleafTemplateEngine engine) {
    return context -> {
      var symbol = context.pathParam("symbol");
      client
        .get(FINNHUB_PORT, FINNHUB_HOST, "/api/v1/stock/recommendation?symbol=" + symbol)
        .putHeader(FINNHUB_HEADER, FINNHUB_API_KEY)
        .send()
        .onFailure(throwable -> log.error("error sending request", throwable))
        .onSuccess(response -> {
            var array = response.bodyAsJsonArray();
            engine
              .render(new JsonObject().put("array", array).put("symbol", symbol), "templates/stock/recommendation.html")
              .onFailure(throwable -> log.error("error rendering template", throwable))
              .onSuccess(buffer ->
                context.response().putHeader("content-type", "text/html").end(buffer)
              );
          }
        );
    };
  }

  private static final String SYMBOLS_ADDRESS = "symbols";

  public static void deploySymbolVerticle(Vertx vertx, WebClient client) {
    vertx
      .deployVerticle(ctx -> {
          vertx.eventBus()
            .consumer(SYMBOLS_ADDRESS)
            .handler(busMessage -> {
                var watch = createStarted();
                client
                  .get(FINNHUB_PORT, FINNHUB_HOST, "/api/v1/stock/symbol?exchange=US")
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

  public static Handler<RoutingContext> stockSymbol(Vertx vertx, TemplateEngine engine) {
    return context -> {
      var watch = createStarted();
      vertx.eventBus()
        .request(SYMBOLS_ADDRESS, null)
        .onFailure(eventBusError -> {
            log.error("failed requesting symbols on event bus", eventBusError);
            if (eventBusError instanceof ReplyException exception) {
              engine
                .render(new JsonObject().put("error", exception.getMessage()), "/templates/common/error.html"
                )
                .onFailure(renderingError -> log.error("error rendering template", renderingError))
                .onSuccess(buffer ->
                  context.response().putHeader("content-type", "text/html").end(buffer)
                );
            }
          }
        )
        .onSuccess(response -> {
            var string = response.body().toString();
            var array = new JsonArray(string);
            engine
              .render(new JsonObject().put("symbols", array), "templates/stock/symbol.html")
              .onFailure(renderingError -> log.error("error rendering error template", renderingError))
              .onSuccess(buffer -> {
                  context.response().putHeader("content-type", "text/html").end(buffer);
                  log.info("Symbol request processing succeeded in {}", watch.elapsed());
                }
              );
          }
        );
    };
  }

  public static Handler<RoutingContext> stockUsaSpending(WebClient client, ThymeleafTemplateEngine engine) {
    return context -> {
      var symbol = context.pathParam("symbol");
      client
        .get(FINNHUB_PORT, FINNHUB_HOST, "/api/v1/stock/usa-spending?symbol=" + symbol + "&from=2020-01-01&to=2025-06-01")
        .putHeader(FINNHUB_HEADER, FINNHUB_API_KEY)
        .send()
        .onFailure(throwable -> log.error("error sending request", throwable))
        .onSuccess(response -> {
            var object = response.bodyAsJsonObject();
            engine
              .render(new JsonObject().put("object", object).put("symbol", symbol), "templates/stock/usa-spending.html")
              .onFailure(throwable -> log.error("error rendering template", throwable))
              .onSuccess(buffer ->
                context.response().putHeader("content-type", "text/html").end(buffer)
              );
          }
        );
    };
  }

  public static Handler<RoutingContext> stockUsptoPatent(WebClient client, ThymeleafTemplateEngine engine) {
    return context -> {
      var symbol = context.pathParam("symbol");
      client
        .get(FINNHUB_PORT, FINNHUB_HOST, "/api/v1/stock/uspto-patent?symbol=" + symbol + "&from=2020-01-01&to=2025-06-01")
        .putHeader(FINNHUB_HEADER, FINNHUB_API_KEY)
        .send()
        .onFailure(throwable -> log.error("error sending request", throwable))
        .onSuccess(response -> {
            var object = response.bodyAsJsonObject();
            engine
              .render(new JsonObject().put("object", object).put("symbol", symbol), "templates/stock/uspto-patent.html")
              .onFailure(throwable -> log.error("error rendering template", throwable))
              .onSuccess(buffer ->
                context.response().putHeader("content-type", "text/html").end(buffer)
              );
          }
        );
    };
  }

  public static Handler<RoutingContext> stockVisaApplication(WebClient client, ThymeleafTemplateEngine engine) {
    return context -> {
      var symbol = context.pathParam("symbol");
      client
        .get(FINNHUB_PORT, FINNHUB_HOST, "/api/v1/stock/visa-application?symbol=" + symbol)
        .putHeader(FINNHUB_HEADER, FINNHUB_API_KEY)
        .send()
        .onFailure(throwable -> log.error("error sending request", throwable))
        .onSuccess(response -> {
            var object = response.bodyAsJsonObject();
            engine
              .render(new JsonObject().put("object", object).put("symbol", symbol), "templates/stock/visa-application.html")
              .onFailure(throwable -> log.error("error rendering template", throwable))
              .onSuccess(buffer ->
                context.response().putHeader("content-type", "text/html").end(buffer)
              );
          }
        );
    };
  }
}
