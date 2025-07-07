package bbb.vertx_demo;

import io.vertx.core.Future;
import io.vertx.core.VerticleBase;
import lombok.extern.slf4j.Slf4j;

import static java.lang.String.format;

@Slf4j
public final class MainVerticle extends VerticleBase {

  private static final String VERSION = System.getenv("TAG");

  @Override
  public Future<?> start() {
    log.info("config: {}", config().encodePrettily());
    int port = config().getInteger("http.port", 8080);
    return
      vertx
        .createHttpServer()
        .requestHandler(request -> {
            if (log.isTraceEnabled())
              log.trace("Received request: {}", request.uri());
            request
              .response()
              .putHeader("content-type", "text/plain")
              .end(format("Hello from Vert.x Demo, version %s!", VERSION));
          }
        )
        .listen(port)
        .onSuccess(http ->
          log.info("HTTP server started on internal port {}", port)
        )
        .onFailure(throwable -> {
            log.error("Could not start a HTTP server", throwable);
          }
        );
  }
}
