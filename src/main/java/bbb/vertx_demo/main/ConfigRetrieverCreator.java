package bbb.vertx_demo.main;

import io.vertx.config.ConfigRetriever;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;

@Slf4j
public enum ConfigRetrieverCreator {

  ;

  public static ConfigRetriever createConfigRetriever
    (
      Vertx vertx,
      JsonObject configServer
    ) {
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
    return ConfigRetriever
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
      )
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
      );
  }
}
