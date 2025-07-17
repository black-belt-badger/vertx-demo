package bbb.vertx_demo.main;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.atomic.AtomicReference;

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
    return vertx
      .createHttpServer()
      .requestHandler(request -> {
          if (log.isTraceEnabled())
            log.trace("Received request: {}", request.uri());
          request
            .response()
            .putHeader("content-type", "text/plain")
            .end(
              format("Hello from Vert.x Demo, version '%s', config '%s'!", VERSION, configServerVersionRef.get())
            );
        }
      )
      .listen(port, host)
      .onSuccess(server ->
        log.info("HTTP server started on internal {}:{}", host, port))
      .onFailure(throwable ->
        log.error("HTTP server failed to start on internal {}:{}", host, port, throwable));
  }
}
