package bbb.vertx_demo.main.db;

import io.vertx.core.Future;
import io.vertx.core.VerticleBase;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;
import io.vertx.pgclient.PgConnection;
import io.vertx.redis.client.RedisAPI;
import io.vertx.sqlclient.Tuple;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static bbb.vertx_demo.main.FinnHubApi.CALENDAR_IPO;
import static bbb.vertx_demo.main.db.NewsUpdater.EPOCH_MILLIS;
import static bbb.vertx_demo.main.http_server.HttpServerStarter.*;
import static io.vertx.core.Future.succeededFuture;

@Slf4j
@RequiredArgsConstructor
public final class IpoUpdater extends VerticleBase {

  public static final String REDIS_KEY = "ipos";
  public static final String IPOS_EPOCH_MILLIS = "ipos-epoch-millis";

  private final WebClient client;
  private final PgConnection pgConnection;
  private final RedisAPI redisAPI;
  private final Duration delay;

  @Override
  public Future<?> start() throws Exception {
    return succeededFuture(vertx
      .setPeriodic(delay.toMillis(), handler -> {
          log.info("IPO updater started, handler {}", handler);
          client
            .get(FINNHUB_PORT, FINNHUB_HOST, CALENDAR_IPO)
            .putHeader(FINNHUB_HEADER, FINNHUB_API_KEY)
            .send()
            .onFailure(throwable -> log.error("Error getting {}", CALENDAR_IPO, throwable))
            .onSuccess(response -> {
                var object = response.bodyAsJsonObject();
                var array = object.getJsonArray("ipoCalendar");
                var values = array.stream()
                  .map(element -> (JsonObject) element)
                  .map(obj -> {
                      var dateString = obj.getString("date");
                      var date = LocalDate.parse(dateString);
                      var exchange = obj.getString("exchange");
                      var name = obj.getString("name");
                      var numberOfShares = obj.getLong("numberOfShares");
                      var price = obj.getString("price");
                      var status = obj.getString("status");
                      var symbol = obj.getString("symbol");
                      var totalSharesValue = obj.getLong("total_shares_value");
                      return Tuple.of(date, exchange, name, numberOfShares, price, status, symbol, totalSharesValue);
                    }
                  ).toList();
                var insert = """
                  INSERT INTO finnhub.calendar_ipo
                    (date, exchange, name, number_of_shares, price, status, symbol, total_shares_value)
                  VALUES ($1, $2, $3, $4, $5, $6, $7, $8)
                  ON CONFLICT ON CONSTRAINT all_fields_unique DO NOTHING
                  """;
                pgConnection
                  .preparedQuery(insert)
                  .executeBatch(values)
                  .onFailure(throwable -> log.error("Error inserting to finnhub.calendar_ipo ", throwable))
                  .onSuccess(rowSet ->
                    pgConnection
                      .preparedQuery("REFRESH MATERIALIZED VIEW finnhub.calendar_ipo_parsed")
                      .execute()
                      .onFailure(throwable -> log.error("Error refreshing finnhub.calendar_ipo_parsed", throwable))
                      .onSuccess(result -> {
                          log.info("Refreshed finnhub.calendar_ipo_parsed");
                          long epochMillis = Instant.now().toEpochMilli();
                          var value = Long.toString(epochMillis);
                          var args = List.of(EPOCH_MILLIS, IPOS_EPOCH_MILLIS, value);
                          redisAPI.hset(args)
                            .onFailure(throwable -> log.error("error setting epoch millis {}", IPOS_EPOCH_MILLIS, throwable))
                            .onSuccess(result2 -> log.info("epoch millis {} updated", IPOS_EPOCH_MILLIS));
                        }
                      )
                  ).flatMap(rowSet -> {
                      var keys = List.of(REDIS_KEY);
                      return redisAPI
                        .del(keys)
                        .onFailure(throwable -> log.error("Error deleting {} form Redis", REDIS_KEY, throwable))
                        .onSuccess(value -> log.info("Deleted {} from Redis", REDIS_KEY));
                    }
                  );
              }
            );
        }
      )
    ).flatMap(timerId ->
      pgConnection
        .notificationHandler(notification -> {
            log.info("Inserted to finnhub.calendar_ipo inserts");
            pgConnection
              .preparedQuery("REFRESH MATERIALIZED VIEW finnhub.calendar_ipo_parsed")
              .execute()
              .onFailure(throwable -> log.error("Error refreshing finnhub.calendar_ipo_parsed", throwable))
              .onSuccess(result -> log.info("Refreshed finnhub.calendar_ipo_parsed"))
              .flatMap(rowSet -> {
                  long epochMillis = Instant.now().toEpochMilli();
                  var value = Long.toString(epochMillis);
                  var args = List.of(EPOCH_MILLIS, IPOS_EPOCH_MILLIS, value);
                  return redisAPI.hset(args)
                    .onFailure(throwable -> log.error("error setting epoch millis {}", REDIS_KEY, throwable))
                    .onSuccess(result -> log.info("epoch millis {} updated", REDIS_KEY));
                }
              );
          }
        )
        .query("LISTEN calendar_ipo_change_channel")
        .execute()
        .onFailure(throwable -> log.error("Error listening on calendar_ipo_change_channel", throwable))
        .onSuccess(rows -> log.info("Created listener for finnhub.calendar_ipo inserts"))
    );
  }
}
