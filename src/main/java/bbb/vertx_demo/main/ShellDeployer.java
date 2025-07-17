package bbb.vertx_demo.main;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.shell.ShellVerticle;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public enum ShellDeployer {

  ;

  public static Future<String> deployShell
    (
      Vertx vertx,
      String host,
      int port
    ) {
    return vertx
      .deployVerticle(
        ShellVerticle.class,
        new DeploymentOptions()
          .setConfig(
            new JsonObject()
              .put("telnetOptions",
                new JsonObject()
                  .put("host", host)
                  .put("port", port)
              )
          )
      )
      .onSuccess(id ->
        log.info("Deploying shell succeeded {}:{} with id {}", host, port, id))
      .onFailure(throwable ->
        log.error("Deploying shell failed {}:{}", host, port, throwable)
      );
  }

}
