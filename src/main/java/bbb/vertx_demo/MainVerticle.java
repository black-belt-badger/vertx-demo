package bbb.vertx_demo;

import io.vertx.config.ConfigRetriever;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.VerticleBase;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.shell.ShellVerticle;
import io.vertx.ext.shell.command.CommandBuilder;
import io.vertx.ext.shell.command.CommandRegistry;
import lombok.extern.slf4j.Slf4j;

import javax.management.*;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicReference;

import static io.vertx.core.Future.failedFuture;
import static io.vertx.core.Future.succeededFuture;
import static java.lang.String.format;
import static java.lang.System.getenv;
import static java.lang.management.ManagementFactory.getPlatformMBeanServer;
import static java.util.Optional.ofNullable;

@Slf4j
public final class MainVerticle extends VerticleBase {

  private static final String VERSION = ofNullable(getenv("VERSION")).orElse("unknown");

  @Override
  public Future<?> start() {
    var starting = config();
    log.info("Start config: {}", starting.encodePrettily());
    var configServerHttpHost = starting.getString("config-server.http.host", "localhost");
    var configServerHttpPort = starting.getInteger("config-server.http.port", 8887);
    var configServerHttpPath = starting.getString("config-server.http.path", "/conf.json");
    var configServerHttpScanPeriodString = starting.getString("config-server.scan-period", "PT5S");
    var configServerHttpScanPeriod = Duration.parse(configServerHttpScanPeriodString);
    var retriever =
      ConfigRetriever.create(vertx,
        new ConfigRetrieverOptions()
          .setScanPeriod(configServerHttpScanPeriod.toMillis())
          .addStore(
            new ConfigStoreOptions()
              .setType("http")
              .setOptional(true)
              .setConfig(
                new JsonObject()
                  .put("host", configServerHttpHost)
                  .put("port", configServerHttpPort)
                  .put("path", configServerHttpPath)
              )
          )
      );
    return retriever
      .getConfig().map(retrieved -> {
          log.info("Retrieved config: {}", retrieved.encodePrettily());
          var merged = retrieved.mergeIn(starting);
          log.info("Merged config: {}", merged.encodePrettily());
          var httpHost = merged.getString("http.host", "0.0.0.0");
          int httpPort = merged.getInteger("http.port", 8080);
          var telnetHost = merged.getString("telnet.host", "0.0.0.0");
          int telnetPort = merged.getInteger("telnet.port", 5000);
          var configServerVersion = starting.getString("config-server.version", "compiled default value");
          var configServerVersionRef = new AtomicReference<String>(configServerVersion);
          retriever.listen(change -> {
              var previous = change.getPreviousConfiguration();
              var next = change.getNewConfiguration();
              log.info("Config changed from {} to {}", previous, next);
              var newConfigServerVersion = next.getString("config-server.version");
              if (newConfigServerVersion != null)
                configServerVersionRef.set(newConfigServerVersion);
            }
          );
          return vertx
            .createHttpServer()
            .requestHandler(request -> {
                if (log.isTraceEnabled())
                  log.trace("Received request: {}", request.uri());
                request
                  .response()
                  .putHeader("content-type", "text/plain")
                  .end(format("Hello from Vert.x Demo, version '%s', config '%s'!", VERSION, configServerVersionRef.get()));
              }
            )
            .listen(httpPort, httpHost)
            .onSuccess(httpServer -> log.info("HTTP server started on internal {}:{}", httpHost, httpPort))
            .onFailure(throwable -> log.error("HTTP server failed to start on internal {}:{}", httpHost, httpPort, throwable))
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
            .onFailure(throwable -> log.error("Shell Verticle failed to deploy on internal {}:{}", telnetHost, telnetPort, throwable))
            .flatMap(ignored ->
              CommandRegistry
                .getShared(vertx)
                .registerCommand(
                  CommandBuilder
                    .command("print-config")
                    .processHandler(process ->
                      process
                        .write("config: ")
                        .write(starting.encodePrettily())
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
      );
  }
}
