package bbb.vertx_demo;

import bbb.vertx_demo.main.RedisHelper;
import io.vertx.core.Future;
import io.vertx.core.VerticleBase;
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
                  var pgConnectOptions = pgConnectOptions(config);
                  return PgConnection.connect(vertx, pgConnectOptions)
                    .onSuccess(postgresConnectionSuccess(checks))
                    .onFailure(postgresConnectionFailure(checks))
                    .flatMap(pgConnection -> {
                        var amqp = config.getJsonObject("amqp");
                        return deployReceiverAndSender(vertx, checks, amqp)
                          .flatMap(nothing -> {
                              registerExampleMBean(checks);
                              deployShell(vertx, checks, config);
                              registerCommandPrintConfig(vertx, checks, config);
                              return startHttpServers(vertx, checks, redisAPI, redisConnection, pgConnection, config);
                            }
                          );
                      }
                    );
                }
              );
          }
        );
  }
}
