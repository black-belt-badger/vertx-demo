package bbb.vertx_demo;

import io.vertx.core.Future;
import io.vertx.core.VerticleBase;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.healthchecks.HealthCheckHandler;
import io.vertx.redis.client.Redis;
import io.vertx.redis.client.RedisAPI;
import io.vertx.redis.client.RedisOptions;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.atomic.AtomicReference;

import static bbb.vertx_demo.main.AmqpDeployer.deployReceiverAndSender;
import static bbb.vertx_demo.main.CommandRegistrator.registerCommand;
import static bbb.vertx_demo.main.ConfigRetrieverCreator.retrieveAndMerge;
import static bbb.vertx_demo.main.MBeanRegistrator.registerMBean;
import static bbb.vertx_demo.main.PostgresConnectionStarter.connectToPostgres;
import static bbb.vertx_demo.main.ShellDeployer.deployShell;
import static bbb.vertx_demo.main.http_server.HttpServerStarter.startHttpServer;

@Slf4j
public final class MainVerticle extends VerticleBase {

  @Override
  public Future<?> start() {
    var starting = config();
    log.info("Starting config: {}", starting.encodePrettily());
    var configServerVersion = new AtomicReference<String>();
    var checks = HealthCheckHandler.create(vertx);
    return
      retrieveAndMerge(vertx, checks, starting, configServerVersion)
        .map(merged -> {
            var redisHost = merged.getString("redis.host", "0.0.0.0");
            int redisPort = merged.getInteger("redis.port", 6379);
            return Redis
              .createClient(vertx, new RedisOptions().addConnectionString("redis://" + redisHost + ":" + redisPort))
              .connect()
              .onSuccess(conn -> log.info("Connected to Redis server"))
              .onFailure(throwable -> log.error("Failed to connect to Redis server", throwable))
              .flatMap(redisConnection -> {
                  var keyPath = merged.getString("key-path", "security/smooth-all/server.key.pem");
                  var certPath = merged.getString("cert-path", "security/smooth-all/server.cert.pem");
                  log.info("Key path: {}", keyPath);
                  log.info("Cert path: {}", certPath);
                  var httpsHost = merged.getString("https.host", "0.0.0.0");
                  int httpsPort = merged.getInteger("https.port", 8443);
                  var httpHost = merged.getString("http.host", "0.0.0.0");
                  int httpPort = merged.getInteger("http.port", 8080);
                  var redisAPI = RedisAPI.api(redisConnection);
                  var cache = merged.getJsonObject("cache", new JsonObject());
                  return startHttpServer(vertx, checks, keyPath, certPath, httpsHost, httpsPort, httpHost, httpPort, redisAPI, redisConnection, cache)
                    .flatMap(ignored -> {
                        var host =
                          merged.getString("telnet.host", "0.0.0.0");
                        int port =
                          merged.getInteger("telnet.port", 5000);
                        return deployShell(vertx, checks, host, port);
                      }
                    )
                    .flatMap(ignored ->
                      registerCommand(vertx, checks, merged)
                    )
                    .flatMap(ignored ->
                      registerMBean(checks)
                    )
                    .andThen(ignored -> {
                        var config =
                          merged.getJsonObject("postgres", new JsonObject());
                        connectToPostgres(vertx, checks, config);
                      }
                    )
                    .andThen(ignored -> {
                        var config = merged.getJsonObject("amqp", new JsonObject());
                        log.info("Amqp config: {}", config.encodePrettily());
                        deployReceiverAndSender(vertx, checks, config);
                      }
                    )
                    .flatMap(ignored -> Redis
                      .createClient(vertx)
                      .connect()
                    )
                    ;
                }
              );
          }
        );
  }
}
