package bbb.vertx_demo.main;

import io.vertx.config.ConfigRetriever;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
public enum ConfigRetrieverCreator {

  ;

  public static Future<JsonObject> retrieveAndMerge
    (
      Vertx vertx,
      JsonObject starting,
      AtomicReference<String> configServerVersion
    ) {
    var configServer = starting
      .getJsonObject("config-server", new JsonObject());
    var host = configServer.getString("host", "localhost");
    log.info("Config server host: {}", host);
    var port = configServer.getInteger("port", 8887);
    log.info("Config server port: {}", port);
    var path = configServer.getString("path", "/conf.json");
    log.info("Config server path: {}", path);
    var scanPeriodString = configServer.getString("scan-period", "PT30S");
    log.info("Config server scan period string: {}", scanPeriodString);
    var scanPeriod = Duration.parse(scanPeriodString);
    log.info("Config server scan period: {}", scanPeriod);
    var retriever =
      ConfigRetriever
        .create(
          vertx,
          new ConfigRetrieverOptions()
            .setScanPeriod(
              scanPeriod.toMillis()
            )
            .addStore(
              new ConfigStoreOptions()
                .setType("http")
                .setOptional(true)
                .setConfig(
                  new JsonObject()
                    .put("host", host)
                    .put("port", port)
                    .put("path", path)
                    .put("headers",
                      new JsonObject()
                        .put("Accept", "application/json")
                    )
                )
            )
        );
    return
      retriever
        .setBeforeScanHandler(ignored -> {
            if (log.isDebugEnabled())
              log.debug("About to scan config");
          }
        )
        .setConfigurationProcessor(config -> {
            if (log.isDebugEnabled())
              log.debug("Processing config: {}", config.encodePrettily());
            return config;
          }
        )
        .getConfig()
        .map(retrieved -> {
            log.info("Retrieved config: {}", retrieved.encodePrettily());
            var merged =
              retrieved.mergeIn(starting);
            log.info("Merged config: {}", merged.encodePrettily());
            var configVersion =
              merged
                .getJsonObject("config-server", new JsonObject())
                .getString("version", "compiled default value");
            configServerVersion.set(configVersion);
            retriever
              .listen(change -> {
                  var previous = change.getPreviousConfiguration();
                  var next = change.getNewConfiguration();
                  log.info("Config changed from {} to {}", previous.encodePrettily(), next.encodePrettily());
                  var newConfigVersion =
                    next
                      .getJsonObject("config-server", new JsonObject())
                      .getString("version");
                  if (newConfigVersion != null)
                    configServerVersion.set(newConfigVersion);
                }
              );
            return merged;
          }
        );
  }
}
