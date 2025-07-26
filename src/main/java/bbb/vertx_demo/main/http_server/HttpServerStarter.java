package bbb.vertx_demo.main.http_server;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.core.net.PemKeyCertOptions;
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

  private static final String HTTPS_WEB_SERVER_STARTED = "https-web-server-started";
  private static final String HTTPS_WEB_SERVER_ONLINE = "https-web-server-online";
  private static final String HTTP_WEB_SERVER_STARTED = "http-web-server-started";
  public static final int FINNHUB_PORT = 80;
  public static final String FINNHUB_HOST = "finnhub.io";
  public static final String FINNHUB_HEADER = "X-Finnhub-Token";
  public static final String FINNHUB_API_KEY = "d1uqv0pr01qletnb7080d1uqv0pr01qletnb708g";

  public static Future<HttpServer> startHttpServer
    (
      Vertx vertx,
      HealthCheckHandler checks,
      String keyPath,
      String certPath,
      String httpsHost,
      int httpsPort,
      String httpHost,
      int httpPort,
      RedisAPI redisApi,
      RedisConnection redisConnection,
      JsonObject cache
    ) {
    var httpRouter = Router.router(vertx);
    httpRouter.get("/").handler(context -> {
        var request = context.request();
        log.info("authority {}", request.authority());
        log.info("absolute URI: {}", request.absoluteURI());
        log.info("is SSL: {}", request.isSSL());
        var hostHeader = request.getHeader("Host");
        var host = hostHeader.contains(":") ? hostHeader.split(":")[0] : hostHeader;
        var path = request.path();
        var query = request.query();
        var fullPath = path + (query != null ? "?" + query : "");
        var redirectUrl = "https://" + host + ":" + httpsPort + fullPath;
        log.info("Redirecting to: {}", redirectUrl);
        context.response()
          .setStatusCode(301)
          .putHeader("Location", redirectUrl)
          .end();
      }
    );
    var httpsRouter = Router.router(vertx);
    httpsRouter.route().handler(ctx -> {
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
    httpsRouter.route("/*").handler(StaticHandler.create("webroot"));
    httpsRouter.route("/favicon.png").handler(StaticHandler.create());
    httpsRouter.get("/health").handler(checks.register(HTTPS_WEB_SERVER_ONLINE, Promise::succeed));
    var engine = ThymeleafTemplateEngine.create(vertx);
    var webClient = WebClient.create(vertx);
    var home = cache.getJsonObject("home", new JsonObject());
    httpsRouter.get("/").handler(home(engine, redisApi, redisConnection, home));
    var countries = cache.getJsonObject("countries", new JsonObject());
    httpsRouter.get("/countries").handler(countries(webClient, engine, redisApi, redisConnection, countries));
    var cryptoExchanges = cache.getJsonObject("crypto-exchanges", new JsonObject());
    httpsRouter.get("/crypto/exchange").handler(cryptoExchange(webClient, engine, redisApi, redisConnection, cryptoExchanges));
    var cryptoSymbols = cache.getJsonObject("crypto-exchanges", new JsonObject());
    httpsRouter.get("/crypto/symbol/:exchange").handler(cryptoSymbol(webClient, engine, redisApi, redisConnection, cryptoSymbols));
    var fdaCalendar = cache.getJsonObject("fda-calendar", new JsonObject());
    httpsRouter.get("/fda-advisory-committee-calendar").handler(fdaAdvisoryCommitteeCalendar(webClient, engine, redisApi, redisConnection, fdaCalendar));
    var forexExchanges = cache.getJsonObject("forex-exchanges", new JsonObject());
    httpsRouter.get("/forex/exchange").handler(forexExchange(webClient, engine, redisApi, redisConnection, forexExchanges));
    var forexSymbols = cache.getJsonObject("forex-exchanges", new JsonObject());
    httpsRouter.get("/forex/symbol/:exchange").handler(forexSymbol(webClient, engine, redisApi, redisConnection, forexSymbols));
    var ipoCalendar = cache.getJsonObject("ipo-calendar", new JsonObject());
    httpsRouter.get("/ipo-calendar").handler(ipoCalendar(webClient, engine, redisApi, redisConnection, ipoCalendar));
    var news = cache.getJsonObject("news", new JsonObject());
    httpsRouter.get("/news/:category").handler(news(webClient, engine, redisApi, redisConnection, news));
    httpsRouter.get("/stock/earnings/:symbol").handler(stockEarnings(webClient, engine));
    httpsRouter.get("/stock/filings/:symbol").handler(stockFilings(webClient, engine));
    httpsRouter.get("/stock/financials-reported/:symbol").handler(stockFinancialsReported(webClient, engine));
    httpsRouter.get("/stock/insider-sentiment/:symbol").handler(stockInsiderSentiment(webClient, engine));
    httpsRouter.get("/stock/insider-transactions/:symbol").handler(stockInsiderTransactions(webClient, engine));
    httpsRouter.get("/stock/market-holiday/:exchange").handler(stockMarketHoliday(webClient, engine));
    var profile2 = cache.getJsonObject("profile2", new JsonObject());
    httpsRouter.get("/stock/profile2/:symbol").handler(stockProfile2(webClient, engine, redisApi, redisConnection, profile2));
    httpsRouter.get("/stock/recommendation/:symbol").handler(stockRecommendation(webClient, engine));
    deploySymbolVerticle(vertx, webClient);
    var stockSymbols = cache.getJsonObject("stock-symbols", new JsonObject());
    httpsRouter.get("/stock/symbol/:exchange").handler(stockSymbol(vertx, engine, redisApi, redisConnection, stockSymbols));
    httpsRouter.get("/stock/usa-spending/:symbol").handler(stockUsaSpending(webClient, engine));
    httpsRouter.get("/stock/uspto-patent/:symbol").handler(stockUsptoPatent(webClient, engine));
    httpsRouter.get("/stock/visa-application/:symbol").handler(stockVisaApplication(webClient, engine));
    return
      vertx
        .createHttpServer(
          new HttpServerOptions().setSsl(true).setKeyCertOptions(
            new PemKeyCertOptions().setKeyPath(keyPath).setCertPath(certPath)
          )
        )
        .requestHandler(httpsRouter)
        .listen(httpsPort, httpsHost)
        .onComplete(ar -> {
            if (ar.succeeded()) checks.register(HTTPS_WEB_SERVER_STARTED, Promise::succeed);
            else checks.register(HTTPS_WEB_SERVER_STARTED, promise ->
              promise.complete(KO(), ar.cause())
            );
            if (ar.succeeded()) log.info("HTTPS {}:{}", httpsHost, httpsPort);
            else log.error("HTTPS failed {}:{}", httpsHost, httpsPort, ar.cause());
          }
        )
        .flatMap(ignored ->
          vertx
            .createHttpServer(new HttpServerOptions())
            .requestHandler(httpRouter)
            .listen(httpPort, httpHost)
        )
        .onComplete(ar -> {
            if (ar.succeeded()) checks.register(HTTP_WEB_SERVER_STARTED, Promise::succeed);
            else checks.register(HTTP_WEB_SERVER_STARTED, promise ->
              promise.complete(KO(), ar.cause())
            );
            if (ar.succeeded()) log.info("HTTP {}:{}", httpHost, httpPort);
            else log.error("HTTP failed {}:{}", httpHost, httpPort, ar.cause());
          }
        );
  }
}
