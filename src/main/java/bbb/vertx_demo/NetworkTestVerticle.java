package bbb.vertx_demo;

import io.vertx.core.Future;
import io.vertx.core.VerticleBase;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.net.PemKeyCertOptions;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class NetworkTestVerticle extends VerticleBase {

  private static final String WILDCARD = "0.0.0.0";
  private static final String LOOPBACK = "127.0.0.1";
  private static final String LOCALHOST = "localhost";
  private static final String HTTP_HOST = WILDCARD;
  private static final String HTTPS_HOST = WILDCARD;
  private static final int HTTP_PORT = 18080;
  private static final int HTTPS_PORT = 18443;

  @Override
  public Future<?> start() {

    var config = config();

    var httpRouter = Router.router(vertx);
    httpRouter.get("/").handler(context -> {
        logRoutingContext(context);
        var request = context.request();
        var hostHeader = request.getHeader("Host");
        var hostOnly = hostHeader.contains(":") ? hostHeader.split(":")[0] : hostHeader;
        var path = request.path();
        var query = request.query();
        var fullPath = path + (query != null ? "?" + query : "");
        var redirectUrl = "https://" + hostOnly + ":" + HTTPS_PORT + fullPath;
        log.info("Redirecting to: {}", redirectUrl);
        context.response()
          .setStatusCode(301)
          .putHeader("Location", redirectUrl)
          .end();
      }
    );
    var httpOptions = new HttpServerOptions();
    var httpServer =
      vertx.createHttpServer(httpOptions).requestHandler(httpRouter);

    var httpsRouter = Router.router(vertx);
    httpsRouter.get("/").handler(context -> {
        logRoutingContext(context);
        context.response()
          .putHeader("Content-Type", "text/plain")
          .end("HTTPS server works" + System.lineSeparator());
      }
    );

    var keyPath = config.getString("key-path", "security/smooth-all/server.key.pem");
    var certPath = config.getString("cert-path", "security/smooth-all/server.cert.pem");
    log.info("Effective key path: {}", keyPath);
    log.info("Effective cert path: {}", certPath);

    var keyCertOptions =
      new PemKeyCertOptions().setKeyPath(keyPath).setCertPath(certPath);
    var httpsOptions =
      new HttpServerOptions().setSsl(true).setKeyCertOptions(keyCertOptions);
    var httpsServer =
      vertx.createHttpServer(httpsOptions).requestHandler(httpsRouter);

    return
      httpServer
        .listen(HTTP_PORT, HTTP_HOST)
        .onFailure(throwable ->
          log.error("HTTP failed {}:{}", HTTP_HOST, HTTP_PORT, throwable)
        )
        .onSuccess(http ->
          log.info("HTTP {}:{}", HTTP_HOST, HTTP_PORT)
        )
        .flatMap(http ->
          httpsServer
            .listen(HTTPS_PORT, HTTP_HOST)
            .onFailure(throwable ->
              log.error("HTTPS failed {}:{}", HTTPS_HOST, HTTPS_PORT, throwable)
            )
            .onSuccess(https ->
              log.info("HTTPS {}:{}", HTTPS_HOST, HTTPS_PORT)
            )
        );
  }

  private static void logRoutingContext(RoutingContext ctx) {
    var request = ctx.request();
    log.info("authority {}", request.authority());
    log.info("absolute URI: {}", request.absoluteURI());
    log.info("is SSL: {}", request.isSSL());
  }
}
