package bbb.vertx_demo;

import io.vertx.core.Future;
import io.vertx.core.VerticleBase;
import io.vertx.core.json.JsonObject;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.atomic.AtomicReference;

import static bbb.vertx_demo.main.CommandRegistrator.registerCommand;
import static bbb.vertx_demo.main.ConfigRetrieverCreator.createConfigRetriever;
import static bbb.vertx_demo.main.HttpServerStarter.startHttpServer;
import static bbb.vertx_demo.main.MBeanRegistrator.registerMBean;
import static bbb.vertx_demo.main.PostgresConnectionStarter.connectToPostgres;
import static bbb.vertx_demo.main.ShellDeployer.deployShell;

@Slf4j
public final class MainVerticle extends VerticleBase {

  @Override
  public Future<?> start() {
    var starting = config();
    log.info("Start config: {}", starting.encodePrettily());
    var configServerConfig = starting.getJsonObject("config-server", new JsonObject());
    var retriever = createConfigRetriever(vertx, configServerConfig);
    return retriever
      .getConfig()
      .map(retrieved -> {
          log.info("Retrieved config: {}", retrieved.encodePrettily());
          var merged = retrieved.mergeIn(starting);
          log.info("Merged config: {}", merged.encodePrettily());
          var configServerVersion =
            new AtomicReference<>(
              merged
                .getJsonObject("config-server", new JsonObject())
                .getString("version", "compiled default value")
            );
          retriever
            .listen(change -> {
                var previous = change.getPreviousConfiguration();
                var next = change.getNewConfiguration();
                log.info("Config changed from {} to {}", previous.encodePrettily(), next.encodePrettily());
                var newConfigServerVersion =
                  next
                    .getJsonObject("config-server", new JsonObject())
                    .getString("version");
                if (newConfigServerVersion != null)
                  configServerVersion.set(newConfigServerVersion);
              }
            );
          var httpHost = merged.getString("http.host", "0.0.0.0");
          int httpPort = merged.getInteger("http.port", 8080);
          return
            startHttpServer(vertx, configServerVersion, httpPort, httpHost)
              .flatMap(ignored -> {
                  var host =
                    merged.getString("telnet.host", "0.0.0.0");
                  int port =
                    merged.getInteger("telnet.port", 5000);
                  return deployShell(vertx, host, port);
                }
              )
              .flatMap(ignored ->
                registerCommand(vertx, merged)
              )
              .flatMap(ignored ->
                registerMBean()
              )
              .andThen(ignored -> {
                  var config =
                    merged.getJsonObject("postgres", new JsonObject());
                  connectToPostgres(vertx, config);
                }
              );
        }
      );
  }
}
