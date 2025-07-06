package bbb.vertx_demo;

import io.vertx.core.Future;
import io.vertx.core.VerticleBase;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MainVerticle extends VerticleBase {

  @Override
  public Future<?> start() {
    log.info("config: {}", config().encodePrettily());
    int port = config().getInteger("http.port", 8080);
    return
      vertx
        .createHttpServer()
        .requestHandler(request ->
          request
            .response()
            .putHeader("content-type", "text/plain")
            .end("Hello from Vert.x!")
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
