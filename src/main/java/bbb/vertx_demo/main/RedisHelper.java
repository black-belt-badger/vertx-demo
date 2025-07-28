package bbb.vertx_demo.main;

import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.healthchecks.HealthCheckHandler;
import io.vertx.redis.client.RedisConnection;
import io.vertx.redis.client.RedisOptions;
import lombok.extern.slf4j.Slf4j;

import static io.vertx.ext.healthchecks.Status.KO;

@Slf4j
public enum RedisHelper {

  ;

  public static RedisOptions redisOptions(JsonObject config) {
    var redisHost = config.getString("redis.host", "0.0.0.0");
    int redisPort = config.getInteger("redis.port", 6379);
    return new RedisOptions().addConnectionString("redis://" + redisHost + ":" + redisPort);
  }

  private static final String REDIS_CONNECTION = "postgres-connection";

  public static Handler<RedisConnection> redisConnectionSuccess(HealthCheckHandler checks) {
    return redisConnection -> {
      log.info("Connected to Redis server");
      checks.register(REDIS_CONNECTION, Promise::complete);
    };
  }

  public static Handler<Throwable> redisConnectionFailure(HealthCheckHandler checks) {
    return throwable -> {
      log.error("Failed to connect to Postgres server", throwable);
      checks.register(REDIS_CONNECTION, promise -> promise.complete(KO(), throwable));
    };
  }
}
