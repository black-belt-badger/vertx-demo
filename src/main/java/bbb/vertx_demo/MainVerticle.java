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

import static bbb.vertx_demo.main.ConfigRetrieverCreator.retrieveAndMerge;
import static bbb.vertx_demo.main.PostgresHelper.*;
import static bbb.vertx_demo.main.RedisHelper.redisConnectionFailure;
import static bbb.vertx_demo.main.RedisHelper.redisConnectionSuccess;
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
                  var pgConnectOptions = pgConnectOptions(config);
                  return PgConnection.connect(vertx, pgConnectOptions)
                    .onSuccess(postgresConnectionSuccess(checks))
                    .onFailure(postgresConnectionFailure(checks))
                    .flatMap(pgConnection -> {
                        var redisAPI = RedisAPI.api(redisConnection);
                        return startHttpServers(vertx, checks, redisAPI, redisConnection, pgConnection, config);
                      }
                    );

//                    .flatMap(ignored ->
//                      registerCommand(vertx, checks, merged)
//                    )
//                    .flatMap(ignored ->
//                      registerMBean(checks)
//                    )
//                    .andThen(ignored -> {
//                        var config =
//                          merged.getJsonObject("postgres", new JsonObject());
//                        connectToPostgres(vertx, checks, config);
//                      }
//                    )
//                    .andThen(ignored -> {
//                        var config = merged.getJsonObject("amqp", new JsonObject());
//                        log.info("Amqp config: {}", config.encodePrettily());
//                        deployReceiverAndSender(vertx, checks, config);
//                      }
//                    )
//                    .flatMap(ignored -> Redis
//                      .createClient(vertx)
//                      .connect()
//                    )
//                    ;
                }
              );
          }
        );
  }
}
