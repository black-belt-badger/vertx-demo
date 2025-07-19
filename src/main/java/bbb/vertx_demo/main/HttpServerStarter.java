package bbb.vertx_demo.main;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.web.Router;
import lombok.extern.slf4j.Slf4j;

import java.util.Random;
import java.util.concurrent.atomic.AtomicReference;

import static io.vertx.ext.healthchecks.Status.KO;
import static io.vertx.ext.healthchecks.Status.OK;
import static io.vertx.ext.web.healthchecks.HealthCheckHandler.create;
import static java.lang.String.format;
import static java.lang.System.getenv;
import static java.util.Optional.ofNullable;

@Slf4j
public enum HttpServerStarter {

  ;

  private static final String VERSION = ofNullable(getenv("VERSION")).orElse("unknown");

  public static Future<HttpServer> startHttpServer
    (
      Vertx vertx,
      AtomicReference<String> configServerVersionRef,
      int port,
      String host
    ) {
     var router = Router.router(vertx);
     router.get("/health").handler(
    create(vertx).register(
                      "server-online", promise -> {
                          var result = new Random().nextBoolean() ? OK() : KO();
                          promise.complete(result);
                      }
              )
      );
     router.get("/").handler(
              context ->
                      context.response()
                              .putHeader("content-type", "text/plain")
                              .end(
                                      format("Hello from Vert.x Demo, version '%s', config '%s'!", VERSION, configServerVersionRef.get())
                              )

    );
    return vertx
      .createHttpServer()
      .requestHandler(router)
      .listen(port, host)
      .onSuccess(server ->
        log.info("HTTP server started on internal {}:{}", host, port))
      .onFailure(throwable ->
        log.error("HTTP server failed to start on internal {}:{}", host, port, throwable));
  }
}
