package bbb.vertx_demo;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.VerticleBase;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.shell.ShellVerticle;
import io.vertx.ext.shell.command.CommandBuilder;
import io.vertx.ext.shell.command.CommandRegistry;
import lombok.extern.slf4j.Slf4j;

import javax.management.*;
import java.lang.management.ManagementFactory;

import static java.lang.String.format;

@Slf4j
public final class MainVerticle extends VerticleBase {

  private static final String VERSION = System.getenv("TAG");

  @Override
  public Future<?> start() {
    log.info("config: {}", config().encodePrettily());
    int httpPort = config().getInteger("http.port", 8080);
    var telnetHost = config().getString("telnet.host", "0.0.0.0");
    int telnetPort = config().getInteger("telnet.port", 5000);
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
        .listen(httpPort)
        .onSuccess(httpServer -> log.info("HTTP server started on internal port {}", httpPort))
        .onFailure(throwable -> log.error("Could not start a HTTP server", throwable))
        .flatMap(ignored ->
          vertx.deployVerticle(
            ShellVerticle.class,
            new DeploymentOptions()
              .setConfig(
                new JsonObject()
                  .put("telnetOptions",
                    new JsonObject()
                      .put("host", telnetHost)
                      .put("port", telnetPort)
                  )
              )
          )
        )
        .onSuccess(id -> log.info("Shell Verticle deployed on internal {}:{} with id {}", telnetHost, telnetPort, id))
        .onFailure(throwable -> log.error("Could not deploy Shell Verticle", throwable))
        .flatMap(ignored ->
          CommandRegistry
            .getShared(vertx)
            .registerCommand(
              CommandBuilder
                .command("print-config")
                .processHandler(process ->
                  process
                    .write("config: ")
                    .write(config().encodePrettily())
                    .write("\n")
                    .end()
                )
                .build(vertx)
            )
        )
        .onSuccess(command -> log.info("Registered command {}", command.name()))
        .onFailure(throwable -> log.error("Could not register command", throwable))
        .flatMap(ignored -> {
            var mbean = new Example();
            var name = "bbb.vertx_in_docker:type=basic,name=vertx-in-docker";
            log.info("Registering MBean {}", name);
            try {
              var objectName = new ObjectName(name);
              ManagementFactory
                .getPlatformMBeanServer()
                .registerMBean(mbean, objectName);
            } catch (MalformedObjectNameException |
                     InstanceAlreadyExistsException |
                     MBeanRegistrationException |
                     NotCompliantMBeanException e) {
              return Future.failedFuture(e);
            }
            return Future.succeededFuture();
          }
        );
  }
}
