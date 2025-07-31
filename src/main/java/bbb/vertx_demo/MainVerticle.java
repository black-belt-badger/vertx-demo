package bbb.vertx_demo;

import bbb.vertx_demo.main.RedisHelper;
import bbb.vertx_demo.main.db.IpoUpdater;
import bbb.vertx_demo.main.db.NewsUpdater;
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
import static bbb.vertx_demo.main.NewsCategory.*;
import static bbb.vertx_demo.main.PostgresHelper.*;
import static bbb.vertx_demo.main.RedisHelper.redisConnectionFailure;
import static bbb.vertx_demo.main.RedisHelper.redisConnectionSuccess;
import static bbb.vertx_demo.main.ShellCommandHelper.registerCommandPrintConfig;
import static bbb.vertx_demo.main.ShellHelper.deployShell;
import static bbb.vertx_demo.main.http_server.HttpServerStarter.startHttpServers;
import static io.vertx.core.Future.all;
import static io.vertx.core.ThreadingModel.VIRTUAL_THREAD;
import static java.time.Duration.parse;

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
                                        var updater = config.getJsonObject("updater", new JsonObject());
                                        var ipoDelayString = updater.getString("ipo-updater-delay", "PT1H");
                                        var ipoDelay = parse(ipoDelayString);
                                        var ipoUpdater = new IpoUpdater(webClient, pgConnection, redisAPI, ipoDelay);
                                        var options = new DeploymentOptions().setThreadingModel(VIRTUAL_THREAD);
                                        return vertx.deployVerticle(ipoUpdater, options)
                                          .onFailure(throwable -> log.error("Failed to deploy IPO updater", throwable))
                                          .onSuccess(ipoUpdaterId -> log.info("Deployed IPO updater {}", ipoUpdaterId))
                                          .flatMap(ipoUpdaterId -> {
                                              var generalDelayString = updater.getString("news-general-updater-delay", "PT1M");
                                              var generalDelay = parse(generalDelayString);
                                              var general = new NewsUpdater(webClient, pgConnection, redisAPI, "General", generalDelay, "finnhub.news_general", "finnhub.news_general_view", GENERAL);
                                              var generalId = vertx.deployVerticle(general, options);
                                              var forexDelayString = updater.getString("news-forex-updater-delay", "PT1M");
                                              var forexDelay = parse(forexDelayString);
                                              var forex = new NewsUpdater(webClient, pgConnection, redisAPI, "Forex", forexDelay, "finnhub.news_forex", "finnhub.news_forex_view", FOREX);
                                              var forexId = vertx.deployVerticle(forex, options);
                                              var cryptoDelayString = updater.getString("news-crypto-updater-delay", "PT1M");
                                              var cryptoDelay = parse(cryptoDelayString);
                                              var crypto = new NewsUpdater(webClient, pgConnection, redisAPI, "Crypto", cryptoDelay, "finnhub.news_crypto", "finnhub.news_crypto_view", CRYPTO);
                                              var cryptoId = vertx.deployVerticle(crypto, options);
                                              var mergerDelayString = updater.getString("news-general-updater-delay", "PT1M");
                                              var mergerDelay = parse(mergerDelayString);
                                              var merger = new NewsUpdater(webClient, pgConnection, redisAPI, "Merger", mergerDelay, "finnhub.news_merger", "finnhub.news_merger_view", MERGER);
                                              var mergerId = vertx.deployVerticle(merger, options);
                                              return all(generalId, forexId, cryptoId, mergerId);
                                            }
                                          )
                                          .onFailure(throwable -> log.error("Failed to deploy NewsGeneralUpdater", throwable))
                                          .onSuccess(future -> log.info("Deployed News updater {}", future)
                                          );
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
