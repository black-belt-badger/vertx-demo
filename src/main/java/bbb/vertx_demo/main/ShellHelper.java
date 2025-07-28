package bbb.vertx_demo.main;

import io.vertx.core.*;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.shell.ShellVerticle;
import io.vertx.ext.web.healthchecks.HealthCheckHandler;
import lombok.extern.slf4j.Slf4j;

import static io.vertx.ext.healthchecks.Status.KO;

@Slf4j
public enum ShellHelper {

  ;

  public static Future<String> deployShell
    (
      Vertx vertx,
      HealthCheckHandler checks,
      JsonObject config
    ) {
    var host = config.getString("telnet.host", "0.0.0.0");
    int port = config.getInteger("telnet.port", 5000);
    var deploymentOptions = deploymentOptions(host, port);
    return vertx
      .deployVerticle(ShellVerticle.class, deploymentOptions)
      .onSuccess(success(checks, host, port))
      .onFailure(failure(checks, host, port));
  }

  private static DeploymentOptions deploymentOptions(String host, int port) {
    return
      new DeploymentOptions()
        .setConfig(
          new JsonObject()
            .put("telnetOptions",
              new JsonObject()
                .put("host", host)
                .put("port", port)
            )
        );
  }

  private static Handler<String> success(HealthCheckHandler checks, String host, int port) {
    return id -> {
      checks.register("shell-deployment", Promise::succeed);
      log.info("Deploying shell succeeded {}:{} with id {}", host, port, id);
    };
  }

  private static Handler<Throwable> failure(HealthCheckHandler checks, String host, int port) {
    return throwable -> {
      checks.register("shell-deployment", promise -> {
          promise.complete(KO(), throwable);
        }
      );
      log.error("Deploying shell failed {}:{}", host, port, throwable);
    };
  }

}
