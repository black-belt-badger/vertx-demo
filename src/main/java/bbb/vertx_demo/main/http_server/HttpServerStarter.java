package bbb.vertx_demo.main.http_server;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.healthchecks.HealthCheckHandler;
import io.vertx.ext.web.templ.thymeleaf.ThymeleafTemplateEngine;
import lombok.extern.slf4j.Slf4j;

import static bbb.vertx_demo.main.http_server.Handlers.*;
import static bbb.vertx_demo.main.http_server.crypto.CryptoHandlers.cryptoExchange;
import static bbb.vertx_demo.main.http_server.crypto.CryptoHandlers.cryptoSymbol;
import static bbb.vertx_demo.main.http_server.forex.ForexHandlers.forexExchange;
import static bbb.vertx_demo.main.http_server.forex.ForexHandlers.forexSymbol;
import static bbb.vertx_demo.main.http_server.stock.StockHandlers.*;
import static io.vertx.ext.healthchecks.Status.KO;

@Slf4j
public enum HttpServerStarter {

  ;

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
      int port,
      String host
    ) {
    var router = Router.router(vertx);
    router.get("/health").handler(
      checks.register(WEB_SERVER_ONLINE, Promise::succeed)
    );
    var engine = ThymeleafTemplateEngine.create(vertx);
    var client = WebClient.create(vertx);
    router.get("/").handler(home(engine));
    router.get("/countries").handler(countries(client, engine));
    router.get("/crypto/exchange").handler(cryptoExchange(client, engine));
    router.get("/crypto/symbol/:exchange").handler(cryptoSymbol(client, engine));
    router.get("/fda-advisory-committee-calendar").handler(fdaAdvisoryCommiteeCalendar(client, engine));
    router.get("/forex/exchange").handler(forexExchange(client, engine));
    router.get("/forex/symbol/:exchange").handler(forexSymbol(client, engine));
    router.get("/ipo-calendar").handler(ipoCalendar(client, engine));
    router.get("/crypto-news").handler(cryptoNews(client, engine));
    router.get("/forex-news").handler(forexNews(client, engine));
    router.get("/general-news").handler(generalNews(client, engine));
    router.get("/merger-news").handler(mergerNews(client, engine));
    router.get("/stock/earnings/:symbol").handler(stockEarnings(client, engine));
    router.get("/stock/filings/:symbol").handler(stockFilings(client, engine));
    router.get("/stock/financials-reported/:symbol").handler(stockFinancialsReported(client, engine));
    router.get("/stock/insider-sentiment/:symbol").handler(stockInsiderSentiment(client, engine));
    router.get("/stock/insider-transactions/:symbol").handler(stockInsiderTransactions(client, engine));
    router.get("/stock/market-holiday/:exchange").handler(stockMarketHoliday(client, engine));
    router.get("/stock/profile2/:symbol").handler(stockProfile2(client, engine));
    router.get("/stock/recommendation/:symbol").handler(stockRecommendation(client, engine));
    router.get("/stock/symbol").blockingHandler(stockSymbol(client, engine));
    router.get("/stock/usa-spending/:symbol").handler(stockUsaSpending(client, engine));
    router.get("/stock/uspto-patent/:symbol").handler(stockUsptoPatent(client, engine));
    router.get("/stock/visa-application/:symbol").handler(stockVisaApplication(client, engine));
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
