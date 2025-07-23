package bbb.vertx_demo.main;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.healthchecks.HealthCheckHandler;
import io.vertx.ext.web.templ.thymeleaf.ThymeleafTemplateEngine;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.atomic.AtomicReference;

import static io.vertx.ext.healthchecks.Status.KO;
import static java.lang.String.format;
import static java.lang.System.getenv;
import static java.util.Optional.ofNullable;

@Slf4j
public enum HttpServerStarter {

  ;

  private static final String VERSION = ofNullable(getenv("VERSION")).orElse("unknown");
  private static final String WEB_SERVER_STARTED = "web-server-started";
  private static final String WEB_SERVER_ONLINE = "web-server-online";
  public static final int FINNHUB_PORT = 80;
  public static final String FINNHUB_HOST = "finnhub.io";
  public static final String FINNHUB_HEADER = "X-Finnhub-Token";
  public static final String FINNHUB_API_KEY = "d1uqv0pr01qletnb7080d1uqv0pr01qletnb708g";

  public static Future<HttpServer> startHttpServer
    (
      Vertx vertx,
      HealthCheckHandler checks,
      AtomicReference<String> configServerVersionRef,
      int port,
      String host
    ) {
    var router = Router.router(vertx);
    router.get("/health").handler(
      checks.register(WEB_SERVER_ONLINE, Promise::succeed)
    );
    var engine = ThymeleafTemplateEngine.create(vertx);
    var client = WebClient.create(vertx);
    router.get("/").handler(context ->
      engine
        .render(new JsonObject().put("version", format("Version: %s", VERSION)), "templates/index.html")
        .onFailure(throwable -> log.error("error rendering template", throwable))
        .onSuccess(buffer ->
          context.response().putHeader("content-type", "text/html").end(buffer)
        )
    );
    router.get("/countries").handler(context ->
      client
        .get(FINNHUB_PORT, FINNHUB_HOST, "/api/v1/country")
        .putHeader(FINNHUB_HEADER, FINNHUB_API_KEY)
        .send()
        .onFailure(throwable -> log.error("error sending request", throwable))
        .onSuccess(response -> {
            var array = response.bodyAsJsonArray();
            engine
              .render(new JsonObject().put("countries", array), "templates/countries.html")
              .onFailure(throwable -> log.error("error rendering template", throwable))
              .onSuccess(buffer ->
                context.response().putHeader("content-type", "text/html").end(buffer)
              );
          }
        )
    );
    router.get("/stock-symbols").handler(context ->
      client
        .get(FINNHUB_PORT, FINNHUB_HOST, "/api/v1/stock/symbol?exchange=US")
        .putHeader(FINNHUB_HEADER, FINNHUB_API_KEY)
        .send()
        .onFailure(throwable -> log.error("error sending request", throwable))
        .onSuccess(response -> {
            var array = response.bodyAsJsonArray();
            engine
              .render(new JsonObject().put("symbols", array), "templates/stock-symbols.html")
              .onFailure(throwable -> log.error("error rendering template", throwable))
              .onSuccess(buffer ->
                context.response().putHeader("content-type", "text/html").end(buffer)
              );
          }
        )
    );
    router.get("/company-profile/:symbol").handler(context -> {
        var symbol = context.pathParam("symbol");
        client
          .get(FINNHUB_PORT, FINNHUB_HOST, "/api/v1/stock/profile2?symbol=" + symbol)
          .putHeader(FINNHUB_HEADER, FINNHUB_API_KEY)
          .send()
          .onFailure(throwable -> log.error("error sending request", throwable))
          .onSuccess(response -> {
              var object = response.bodyAsJsonObject();
              engine
                .render(new JsonObject().put("profile", object), "templates/company-profile.html")
                .onFailure(throwable -> log.error("error rendering template", throwable))
                .onSuccess(buffer ->
                  context.response().putHeader("content-type", "text/html").end(buffer)
                );
            }
          );
      }
    );
    router.get("/stock/earnings/:symbol").handler(context -> {
        var symbol = context.pathParam("symbol");
        client
          .get(FINNHUB_PORT, FINNHUB_HOST, "/api/v1/stock/earnings?symbol=" + symbol)
          .putHeader(FINNHUB_HEADER, FINNHUB_API_KEY)
          .send()
          .onFailure(throwable -> log.error("error sending request", throwable))
          .onSuccess(response -> {
              var array = response.bodyAsJsonArray();
              engine
                .render(new JsonObject().put("array", array).put("symbol", symbol), "templates/stock-earnings.html")
                .onFailure(throwable -> log.error("error rendering template", throwable))
                .onSuccess(buffer ->
                  context.response().putHeader("content-type", "text/html").end(buffer)
                );
            }
          );
      }
    );
    router.get("/stock/financials-reported/:symbol").handler(context -> {
        var symbol = context.pathParam("symbol");
        client
          .get(FINNHUB_PORT, FINNHUB_HOST, "/api/v1/stock/financials-reported?symbol=" + symbol)
          .putHeader(FINNHUB_HEADER, FINNHUB_API_KEY)
          .send()
          .onFailure(throwable -> log.error("error sending request", throwable))
          .onSuccess(response -> {
              var object = response.bodyAsJsonObject();
              engine
                .render(new JsonObject().put("object", object).put("symbol", symbol), "templates/stock-financials-reported.html")
                .onFailure(throwable -> log.error("error rendering template", throwable))
                .onSuccess(buffer ->
                  context.response().putHeader("content-type", "text/html").end(buffer)
                );
            }
          );
      }
    );
    router.get("/stock/visa-application/:symbol").handler(context -> {
        var symbol = context.pathParam("symbol");
        client
          .get(FINNHUB_PORT, FINNHUB_HOST, "/api/v1/stock/visa-application?symbol=" + symbol)
          .putHeader(FINNHUB_HEADER, FINNHUB_API_KEY)
          .send()
          .onFailure(throwable -> log.error("error sending request", throwable))
          .onSuccess(response -> {
              var object = response.bodyAsJsonObject();
              engine
                .render(new JsonObject().put("object", object).put("symbol", symbol), "templates/stock-visa-application.html")
                .onFailure(throwable -> log.error("error rendering template", throwable))
                .onSuccess(buffer ->
                  context.response().putHeader("content-type", "text/html").end(buffer)
                );
            }
          );
      }
    );
    router.get("/stock/insider-sentiment/:symbol").handler(context -> {
        var symbol = context.pathParam("symbol");
        client
          .get(FINNHUB_PORT, FINNHUB_HOST, "/api/v1/stock/insider-sentiment?symbol=" + symbol)
          .putHeader(FINNHUB_HEADER, FINNHUB_API_KEY)
          .send()
          .onFailure(throwable -> log.error("error sending request", throwable))
          .onSuccess(response -> {
              var object = response.bodyAsJsonObject();
              engine
                .render(new JsonObject().put("object", object).put("symbol", symbol), "templates/stock-insider-sentiment.html")
                .onFailure(throwable -> log.error("error rendering template", throwable))
                .onSuccess(buffer ->
                  context.response().putHeader("content-type", "text/html").end(buffer)
                );
            }
          );
      }
    );
    router.get("/stock/insider-transactions/:symbol").handler(context -> {
        var symbol = context.pathParam("symbol");
        client
          .get(FINNHUB_PORT, FINNHUB_HOST, "/api/v1/stock/insider-transactions?symbol=" + symbol)
          .putHeader(FINNHUB_HEADER, FINNHUB_API_KEY)
          .send()
          .onFailure(throwable -> log.error("error sending request", throwable))
          .onSuccess(response -> {
              var object = response.bodyAsJsonObject();
              engine
                .render(new JsonObject().put("object", object).put("symbol", symbol), "templates/stock-insider-transactions.html")
                .onFailure(throwable -> log.error("error rendering template", throwable))
                .onSuccess(buffer ->
                  context.response().putHeader("content-type", "text/html").end(buffer)
                );
            }
          );
      }
    );
    router.get("/forex-exchanges").handler(context ->
      client
        .get(FINNHUB_PORT, FINNHUB_HOST, "/api/v1/forex/exchange")
        .putHeader(FINNHUB_HEADER, FINNHUB_API_KEY)
        .send()
        .onFailure(throwable -> log.error("error sending request", throwable))
        .onSuccess(response -> {
            var array = response.bodyAsJsonArray();
            engine
              .render(new JsonObject().put("exchanges", array), "templates/forex-exchanges.html")
              .onFailure(throwable -> log.error("error rendering template", throwable))
              .onSuccess(buffer ->
                context.response().putHeader("content-type", "text/html").end(buffer)
              );
          }
        )
    );
    router.get("/forex-symbols/:exchange").handler(context -> {
        var exchange = context.pathParam("exchange");
        client
          .get(FINNHUB_PORT, FINNHUB_HOST, "/api/v1/forex/symbol?exchange=" + exchange)
          .putHeader(FINNHUB_HEADER, FINNHUB_API_KEY)
          .send()
          .onFailure(throwable -> log.error("error sending request", throwable))
          .onSuccess(response -> {
              var array = response.bodyAsJsonArray();
              engine
                .render(new JsonObject().put("symbols", array), "templates/forex-symbols.html")
                .onFailure(throwable -> log.error("error rendering template", throwable))
                .onSuccess(buffer ->
                  context.response().putHeader("content-type", "text/html").end(buffer)
                );
            }
          );
      }
    );
    router.get("/crypto-exchanges").handler(context ->
      client
        .get(FINNHUB_PORT, FINNHUB_HOST, "/api/v1/crypto/exchange")
        .putHeader(FINNHUB_HEADER, FINNHUB_API_KEY)
        .send()
        .onFailure(throwable -> log.error("error sending request", throwable))
        .onSuccess(response -> {
            var array = response.bodyAsJsonArray();
            engine
              .render(new JsonObject().put("exchanges", array), "templates/crypto-exchanges.html")
              .onFailure(throwable -> log.error("error rendering template", throwable))
              .onSuccess(buffer ->
                context.response().putHeader("content-type", "text/html").end(buffer)
              );
          }
        )
    );
    router.get("/crypto-symbols/:exchange").handler(context -> {
        var exchange = context.pathParam("exchange");
        client
          .get(FINNHUB_PORT, FINNHUB_HOST, "/api/v1/crypto/symbol?exchange=" + exchange)
          .putHeader(FINNHUB_HEADER, FINNHUB_API_KEY)
          .send()
          .onFailure(throwable -> log.error("error sending request", throwable))
          .onSuccess(response -> {
              var array = response.bodyAsJsonArray();
              engine
                .render(new JsonObject().put("symbols", array), "templates/crypto-symbols.html")
                .onFailure(throwable -> log.error("error rendering template", throwable))
                .onSuccess(buffer ->
                  context.response().putHeader("content-type", "text/html").end(buffer)
                );
            }
          );
      }
    );
    router.get("/fda-advisory-committee-calendar").handler(context ->
      client
        .get(FINNHUB_PORT, FINNHUB_HOST, "/api/v1/fda-advisory-committee-calendar")
        .putHeader(FINNHUB_HEADER, FINNHUB_API_KEY)
        .send()
        .onFailure(throwable -> log.error("error sending request", throwable))
        .onSuccess(response -> {
            var array = response.bodyAsJsonArray();
            engine
              .render(new JsonObject().put("entries", array), "templates/fda-advisory-committee-calendar.html")
              .onFailure(throwable -> log.error("error rendering template", throwable))
              .onSuccess(buffer ->
                context.response().putHeader("content-type", "text/html").end(buffer)
              );
          }
        )
    );
    router.get("/ipo-calendar").handler(context ->
      client
        .get(FINNHUB_PORT, FINNHUB_HOST, "/api/v1/calendar/ipo")
        .putHeader(FINNHUB_HEADER, FINNHUB_API_KEY)
        .send()
        .onFailure(throwable -> log.error("error sending request", throwable))
        .onSuccess(response -> {
            var object = response.bodyAsJsonObject();
            engine
              .render(new JsonObject().put("object", object), "templates/calendar-ipo.html")
              .onFailure(throwable -> log.error("error rendering template", throwable))
              .onSuccess(buffer ->
                context.response().putHeader("content-type", "text/html").end(buffer)
              );
          }
        )
    );
    router.get("/general-news").handler(context ->
      client
        .get(FINNHUB_PORT, FINNHUB_HOST, "/api/v1/news?category=general")
        .putHeader(FINNHUB_HEADER, FINNHUB_API_KEY)
        .send()
        .onFailure(throwable -> log.error("error sending request", throwable))
        .onSuccess(response -> {
            var array = response.bodyAsJsonArray();
            engine
              .render(new JsonObject().put("news", array).put("caption", "General news"), "templates/news.html")
              .onFailure(throwable -> log.error("error rendering template", throwable))
              .onSuccess(buffer ->
                context.response().putHeader("content-type", "text/html").end(buffer)
              );
          }
        )
    );
    router.get("/forex-news").handler(context ->
      client
        .get(FINNHUB_PORT, FINNHUB_HOST, "/api/v1/news?category=forex")
        .putHeader(FINNHUB_HEADER, FINNHUB_API_KEY)
        .send()
        .onFailure(throwable -> log.error("error sending request", throwable))
        .onSuccess(response -> {
            var array = response.bodyAsJsonArray();
            engine
              .render(new JsonObject().put("news", array).put("caption", "Forex news"), "templates/news.html")
              .onFailure(throwable -> log.error("error rendering template", throwable))
              .onSuccess(buffer ->
                context.response().putHeader("content-type", "text/html").end(buffer)
              );
          }
        )
    );
    router.get("/crypto-news").handler(context ->
      client
        .get(FINNHUB_PORT, FINNHUB_HOST, "/api/v1/news?category=crypto")
        .putHeader(FINNHUB_HEADER, FINNHUB_API_KEY)
        .send()
        .onFailure(throwable -> log.error("error sending request", throwable))
        .onSuccess(response -> {
            var array = response.bodyAsJsonArray();
            engine
              .render(new JsonObject().put("news", array).put("caption", "Crypto news"), "templates/news.html")
              .onFailure(throwable -> log.error("error rendering template", throwable))
              .onSuccess(buffer ->
                context.response().putHeader("content-type", "text/html").end(buffer)
              );
          }
        )
    );
    router.get("/merger-news").handler(context ->
      client
        .get(FINNHUB_PORT, FINNHUB_HOST, "/api/v1/news?category=merger")
        .putHeader(FINNHUB_HEADER, FINNHUB_API_KEY)
        .send()
        .onFailure(throwable -> log.error("error sending request", throwable))
        .onSuccess(response -> {
            var array = response.bodyAsJsonArray();
            engine
              .render(new JsonObject().put("news", array).put("caption", "Merger news"), "templates/news.html")
              .onFailure(throwable -> log.error("error rendering template", throwable))
              .onSuccess(buffer ->
                context.response().putHeader("content-type", "text/html").end(buffer)
              );
          }
        )
    );
    return vertx
      .createHttpServer()
      .requestHandler(router)
      .listen(port, host)
      .onSuccess(server -> {
          checks.register(WEB_SERVER_STARTED, Promise::succeed);
          log.info("HTTP server started on internal {}:{}", host, port);
        }
      )
      .onFailure(throwable -> {
          checks.register(WEB_SERVER_STARTED, promise ->
            promise.complete(KO(), throwable)
          );
          log.error("HTTP server failed to start on internal {}:{}", host, port, throwable);
        }
      );
  }
}
