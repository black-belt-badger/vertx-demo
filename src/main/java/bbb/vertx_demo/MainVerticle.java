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

import static io.vertx.core.Future.failedFuture;
import static io.vertx.core.Future.succeededFuture;
import static java.lang.String.format;
import static java.lang.management.ManagementFactory.getPlatformMBeanServer;

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
            var mbean = new Controller();
            var name = "bbb.vertx_demo:type=basic,name=vertx-demo";
            log.info("Registering MBean {}", name);
            try {
              var objectName = new ObjectName(name);
              var instance = getPlatformMBeanServer().registerMBean(mbean, objectName);
              return succeededFuture(instance);
            } catch (MalformedObjectNameException |
                     InstanceAlreadyExistsException |
                     MBeanRegistrationException |
                     NotCompliantMBeanException e) {
              return failedFuture(e);
            }
          }
        );
  }
}
