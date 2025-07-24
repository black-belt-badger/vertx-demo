package bbb.vertx_demo;

import io.vertx.core.Future;
import io.vertx.core.VerticleBase;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.healthchecks.HealthCheckHandler;
import io.vertx.redis.client.Redis;
import io.vertx.redis.client.RedisAPI;
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
            var httpHost = merged.getString("http.host", "0.0.0.0");
            int httpPort = merged.getInteger("http.port", 8080);
            return Redis
              .createClient(vertx)
              .connect()
              .onSuccess(conn -> log.info("Connected to Redis server"))
              .onFailure(throwable -> log.error("Failed to connect to Redis server", throwable))
              .flatMap(redisConnection -> {
                  var redisAPI = RedisAPI.api(redisConnection);
                  return startHttpServer(vertx, checks, httpPort, httpHost, redisAPI, redisConnection)
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
