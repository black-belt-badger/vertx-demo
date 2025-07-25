package bbb.vertx_demo.main.http_server;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.healthchecks.HealthCheckHandler;
import io.vertx.ext.web.templ.thymeleaf.ThymeleafTemplateEngine;
import io.vertx.redis.client.RedisAPI;
import io.vertx.redis.client.RedisConnection;
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
      String host,
      RedisAPI redisApi,
      RedisConnection redisConnection,
      JsonObject cache
    ) {
    var router = Router.router(vertx);
    router.get("/health").handler(checks.register(WEB_SERVER_ONLINE, Promise::succeed));
    var engine = ThymeleafTemplateEngine.create(vertx);
    var webClient = WebClient.create(vertx);
    router.get("/").handler(home(engine));
    var countries = cache.getJsonObject("countries", new JsonObject());
    router.get("/countries").handler(countries(webClient, engine, redisApi, redisConnection, countries));
    router.get("/crypto/exchange").handler(cryptoExchange(webClient, engine));
    router.get("/crypto/symbol/:exchange").handler(cryptoSymbol(webClient, engine));
    router.get("/fda-advisory-committee-calendar").handler(fdaAdvisoryCommiteeCalendar(webClient, engine));
    router.get("/forex/exchange").handler(forexExchange(webClient, engine));
    router.get("/forex/symbol/:exchange").handler(forexSymbol(webClient, engine));
    router.get("/ipo-calendar").handler(ipoCalendar(webClient, engine));
    router.get("/crypto-news").handler(cryptoNews(webClient, engine));
    router.get("/forex-news").handler(forexNews(webClient, engine));
    router.get("/general-news").handler(generalNews(webClient, engine));
    router.get("/merger-news").handler(mergerNews(webClient, engine));
    router.get("/stock/earnings/:symbol").handler(stockEarnings(webClient, engine));
    router.get("/stock/filings/:symbol").handler(stockFilings(webClient, engine));
    router.get("/stock/financials-reported/:symbol").handler(stockFinancialsReported(webClient, engine));
    router.get("/stock/insider-sentiment/:symbol").handler(stockInsiderSentiment(webClient, engine));
    router.get("/stock/insider-transactions/:symbol").handler(stockInsiderTransactions(webClient, engine));
    router.get("/stock/market-holiday/:exchange").handler(stockMarketHoliday(webClient, engine));
    router.get("/stock/profile2/:symbol").handler(stockProfile2(webClient, engine));
    router.get("/stock/recommendation/:symbol").handler(stockRecommendation(webClient, engine));
    deploySymbolVerticle(vertx, webClient);
    router.get("/stock/symbol").handler(stockSymbol(vertx, engine));
    router.get("/stock/usa-spending/:symbol").handler(stockUsaSpending(webClient, engine));
    router.get("/stock/uspto-patent/:symbol").handler(stockUsptoPatent(webClient, engine));
    router.get("/stock/visa-application/:symbol").handler(stockVisaApplication(webClient, engine));
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
