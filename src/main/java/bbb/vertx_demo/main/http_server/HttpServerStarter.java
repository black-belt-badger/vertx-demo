package bbb.vertx_demo.main.http_server;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.ext.web.healthchecks.HealthCheckHandler;
import io.vertx.ext.web.templ.thymeleaf.ThymeleafTemplateEngine;
import io.vertx.redis.client.RedisAPI;
import io.vertx.redis.client.RedisConnection;
import lombok.extern.slf4j.Slf4j;

import static bbb.vertx_demo.main.http_server.Countries.countries;
import static bbb.vertx_demo.main.http_server.FdaAdvisoryCommiteeCalendar.fdaAdvisoryCommitteeCalendar;
import static bbb.vertx_demo.main.http_server.Home.home;
import static bbb.vertx_demo.main.http_server.IpoCalendar.ipoCalendar;
import static bbb.vertx_demo.main.http_server.News.news;
import static bbb.vertx_demo.main.http_server.crypto.CryptoExchanges.cryptoExchange;
import static bbb.vertx_demo.main.http_server.crypto.CryptoSymbols.cryptoSymbol;
import static bbb.vertx_demo.main.http_server.forex.ForexExchanges.forexExchange;
import static bbb.vertx_demo.main.http_server.forex.ForexSymbol.forexSymbol;
import static bbb.vertx_demo.main.http_server.stock.StockEarnings.stockEarnings;
import static bbb.vertx_demo.main.http_server.stock.StockFilings.stockFilings;
import static bbb.vertx_demo.main.http_server.stock.StockFinancialsReported.stockFinancialsReported;
import static bbb.vertx_demo.main.http_server.stock.StockInsiderSentiment.stockInsiderSentiment;
import static bbb.vertx_demo.main.http_server.stock.StockInsiderTransactions.stockInsiderTransactions;
import static bbb.vertx_demo.main.http_server.stock.StockMarketHoliday.stockMarketHoliday;
import static bbb.vertx_demo.main.http_server.stock.StockProfile2.stockProfile2;
import static bbb.vertx_demo.main.http_server.stock.StockRecommendation.stockRecommendation;
import static bbb.vertx_demo.main.http_server.stock.StockSymbol.deploySymbolVerticle;
import static bbb.vertx_demo.main.http_server.stock.StockSymbol.stockSymbol;
import static bbb.vertx_demo.main.http_server.stock.StockUsaSpending.stockUsaSpending;
import static bbb.vertx_demo.main.http_server.stock.StockUsptoPatent.stockUsptoPatent;
import static bbb.vertx_demo.main.http_server.stock.StockVisaApplications.stockVisaApplication;
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
    router.route().handler(ctx -> {
        var path = ctx.normalizedPath();
        if (
          path.equals("/") ||
            path.equals("/robots.txt") ||
            path.endsWith(".png") ||
            path.endsWith(".ico") ||
            path.endsWith(".svg") ||
            path.endsWith(".css") ||
            path.endsWith(".js")
        ) {
          ctx.next();
          return;
        }
        var auth = ctx.request().getHeader("Authorization");
        if (auth != null && auth.startsWith("Basic ")) {
          var base64 = auth.substring("Basic ".length());
          var decoded = new String(java.util.Base64.getDecoder().decode(base64));
          var parts = decoded.split(":", 2);
          if (parts.length == 2 && parts[0].equals("user") && parts[1].equals("pass")) {
            ctx.next();
            return;
          }
        }
        ctx.response()
          .putHeader("WWW-Authenticate", "Basic realm=\"9ROVE\"")
          .setStatusCode(401)
          .end("Unauthorized");
      }
    );
    router.route("/*").handler(StaticHandler.create("webroot"));
    router.route("/favicon.png").handler(StaticHandler.create());
    router.get("/health").handler(checks.register(WEB_SERVER_ONLINE, Promise::succeed));
    var engine = ThymeleafTemplateEngine.create(vertx);
    var webClient = WebClient.create(vertx);
    var home = cache.getJsonObject("home", new JsonObject());
    router.get("/").handler(home(engine, redisApi, redisConnection, home));
    var countries = cache.getJsonObject("countries", new JsonObject());
    router.get("/countries").handler(countries(webClient, engine, redisApi, redisConnection, countries));
    var cryptoExchanges = cache.getJsonObject("crypto-exchanges", new JsonObject());
    router.get("/crypto/exchange").handler(cryptoExchange(webClient, engine, redisApi, redisConnection, cryptoExchanges));
    var cryptoSymbols = cache.getJsonObject("crypto-exchanges", new JsonObject());
    router.get("/crypto/symbol/:exchange").handler(cryptoSymbol(webClient, engine, redisApi, redisConnection, cryptoSymbols));
    var fdaCalendar = cache.getJsonObject("fda-calendar", new JsonObject());
    router.get("/fda-advisory-committee-calendar").handler(fdaAdvisoryCommitteeCalendar(webClient, engine, redisApi, redisConnection, fdaCalendar));
    var forexExchanges = cache.getJsonObject("forex-exchanges", new JsonObject());
    router.get("/forex/exchange").handler(forexExchange(webClient, engine, redisApi, redisConnection, forexExchanges));
    var forexSymbols = cache.getJsonObject("forex-exchanges", new JsonObject());
    router.get("/forex/symbol/:exchange").handler(forexSymbol(webClient, engine, redisApi, redisConnection, forexSymbols));
    var ipoCalendar = cache.getJsonObject("ipo-calendar", new JsonObject());
    router.get("/ipo-calendar").handler(ipoCalendar(webClient, engine, redisApi, redisConnection, ipoCalendar));
    var news = cache.getJsonObject("news", new JsonObject());
    router.get("/news/:category").handler(news(webClient, engine, redisApi, redisConnection, news));
    router.get("/stock/earnings/:symbol").handler(stockEarnings(webClient, engine));
    router.get("/stock/filings/:symbol").handler(stockFilings(webClient, engine));
    router.get("/stock/financials-reported/:symbol").handler(stockFinancialsReported(webClient, engine));
    router.get("/stock/insider-sentiment/:symbol").handler(stockInsiderSentiment(webClient, engine));
    router.get("/stock/insider-transactions/:symbol").handler(stockInsiderTransactions(webClient, engine));
    router.get("/stock/market-holiday/:exchange").handler(stockMarketHoliday(webClient, engine));
    router.get("/stock/profile2/:symbol").handler(stockProfile2(webClient, engine));
    router.get("/stock/recommendation/:symbol").handler(stockRecommendation(webClient, engine));
    deploySymbolVerticle(vertx, webClient);
    var stockSymbols = cache.getJsonObject("stock-symbols", new JsonObject());
    router.get("/stock/symbol/:exchange").handler(stockSymbol(vertx, engine, redisApi, redisConnection, stockSymbols));
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
