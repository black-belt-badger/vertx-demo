package bbb.vertx_demo.main.db;

import io.vertx.core.Future;
import io.vertx.core.VerticleBase;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;
import io.vertx.pgclient.PgConnection;
import io.vertx.sqlclient.Tuple;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;

import static bbb.vertx_demo.main.FinnHubApi.CALENDAR_IPO;
import static bbb.vertx_demo.main.http_server.HttpServerStarter.*;
import static io.vertx.core.Future.succeededFuture;
import static java.time.Duration.ofSeconds;

@Slf4j
@RequiredArgsConstructor
public final class IpoUpdater extends VerticleBase {

  private final WebClient client;
  private final PgConnection pgConnection;

  @Override
  public Future<Void> start() throws Exception {
    vertx.setPeriodic(ofSeconds(10).toMillis(), ofSeconds(10).toMillis(), id -> {
        log.info("IPO updater started, timer ID {}", id);
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
                    .onSuccess(result -> log.info("Refreshed finnhub.calendar_ipo_parsed"))
                );
            }
          );
      }
    );
    return succeededFuture();
  }
}
