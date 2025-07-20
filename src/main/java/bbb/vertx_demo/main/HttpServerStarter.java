package bbb.vertx_demo.main;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.healthchecks.HealthCheckHandler;
import io.vertx.ext.web.templ.thymeleaf.ThymeleafTemplateEngine;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.atomic.AtomicReference;

import static io.vertx.ext.healthchecks.Status.KO;
import static java.lang.System.getenv;
import static java.util.Optional.ofNullable;

@Slf4j
public enum HttpServerStarter {

  ;

  private static final String VERSION = ofNullable(getenv("VERSION")).orElse("unknown");
  private static final String WEB_SERVER_STARTED = "web-server-started";
  private static final String WEB_SERVER_ONLINE = "web-server-online";

  public static Future<HttpServer> startHttpServer
    (
      Vertx vertx,
      HealthCheckHandler checks,
      AtomicReference<String> configServerVersionRef,
      int port,
      String host
    ) {
    var router = Router.router(vertx);
    router.get("/health")
      .handler(
        checks.register(WEB_SERVER_ONLINE, Promise::succeed)
      );
    var engine = ThymeleafTemplateEngine.create(vertx);
    router.get("/")
      .handler(context -> {
          engine
            .render(
              new JsonObject()
                .put("foo", "Foo")
                .put("bar", "Bar")
                .put("version", configServerVersionRef.get()
                ),
              "thymeleaf/example-template.html"
            )
            .onFailure(throwable -> log.error("error rendering template", throwable))
            .onSuccess(buffer -> {
                context.response()
                  .putHeader("content-type", "text/plain")
                  .end(buffer);
              }
            );
        }
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
