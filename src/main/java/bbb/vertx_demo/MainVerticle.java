package bbb.vertx_demo;

import bbb.vertx_demo.main.RedisHelper;
import bbb.vertx_demo.main.db.IpoUpdater;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.VerticleBase;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.healthchecks.HealthCheckHandler;
import io.vertx.pgclient.PgConnection;
import io.vertx.redis.client.Redis;
import io.vertx.redis.client.RedisAPI;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.atomic.AtomicReference;

import static bbb.vertx_demo.main.AmqpHelper.deployReceiverAndSender;
import static bbb.vertx_demo.main.ConfigRetrieverHelper.retrieveAndMerge;
import static bbb.vertx_demo.main.MBeanHelper.registerExampleMBean;
import static bbb.vertx_demo.main.PostgresHelper.*;
import static bbb.vertx_demo.main.RedisHelper.redisConnectionFailure;
import static bbb.vertx_demo.main.RedisHelper.redisConnectionSuccess;
import static bbb.vertx_demo.main.ShellCommandHelper.registerCommandPrintConfig;
import static bbb.vertx_demo.main.ShellHelper.deployShell;
import static bbb.vertx_demo.main.http_server.HttpServerStarter.startHttpServers;
import static io.vertx.core.ThreadingModel.VIRTUAL_THREAD;

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
        .map(config -> {
            var redisOptions = RedisHelper.redisOptions(config);
            return Redis.createClient(vertx, redisOptions).connect()
              .onSuccess(redisConnectionSuccess(checks))
              .onFailure(redisConnectionFailure(checks))
              .flatMap(redisConnection -> {
                  var redisAPI = RedisAPI.api(redisConnection);
                  var postgres = config.getJsonObject("postgres", new JsonObject());
                  var pgConnectOptions = pgConnectOptions(postgres);
                  return PgConnection.connect(vertx, pgConnectOptions)
                    .onSuccess(postgresConnectionSuccess(checks))
                    .onFailure(postgresConnectionFailure(checks))
                    .flatMap(pgConnection ->
                      vertx
                        .executeBlocking(() -> migratePgDatabase(postgres))
                        .onSuccess(postgresMigrationSuccess(checks))
                        .onFailure(postgresMigrationFailure(checks))
                        .flatMap(migrationResult -> {
                            var amqp = config.getJsonObject("amqp");
                            return deployReceiverAndSender(vertx, checks, amqp)
                              .flatMap(nothing -> {
                                  registerExampleMBean(checks);
                                  deployShell(vertx, checks, config);
                                  registerCommandPrintConfig(vertx, checks, config);
                                  var http = config.getJsonObject("http");
                                  return startHttpServers(vertx, checks, redisAPI, redisConnection, pgConnection, http)
                                    .flatMap(httpServer -> {
                                        var webClient = WebClient.create(vertx);
                                        var updater = new IpoUpdater(webClient, pgConnection);
                                        var options = new DeploymentOptions().setThreadingModel(VIRTUAL_THREAD);
                                        return vertx.deployVerticle(updater, options);
                                      }
                                    );
                                }
                              );
                          }
                        )
                    );
                }
              );
          }
        );
  }
}
