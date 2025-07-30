package bbb.vertx_demo.main.db;

import io.vertx.core.Future;
import io.vertx.core.VerticleBase;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;
import io.vertx.pgclient.PgConnection;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import static bbb.vertx_demo.main.FinnHubApi.CALENDAR_IPO;
import static bbb.vertx_demo.main.http_server.HttpServerStarter.*;
import static io.vertx.core.Future.succeededFuture;
import static java.lang.String.format;
import static java.util.stream.Collectors.joining;

@Slf4j
@RequiredArgsConstructor
public final class IpoUpdater extends VerticleBase {

  private final WebClient client;
  private final PgConnection pgConnection;

  @Override
  public Future<Void> start() throws Exception {
    vertx.setPeriodic(0, 10_000, id -> {
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
                    var date = obj.getString("date");
                    var exchange = obj.getString("exchange");
                    var name = obj.getString("name");
                    var numberOfShares = obj.getLong("numberOfShares");
                    var price = obj.getString("price");
                    var status = obj.getString("status");
                    var symbol = obj.getString("symbol");
                    var totalSharesValue = obj.getLong("total_shares_value");
                    return format("('%s', '%s', '%s', %d, '%s', '%s', '%s', %d)", date, exchange, name, numberOfShares, price, status, symbol, totalSharesValue);
                  }
                ).collect(joining(", "));
              var insert =
                format("""
                    INSERT INTO finnhub.calendar_ipo (date, exchange, name, number_of_shares, price, status, symbol, total_shares_value)
                    VALUES %s
                    ON CONFLICT ON CONSTRAINT all_fields_unique DO NOTHING"""
                  , values
                );
              pgConnection
                .preparedQuery(insert)
                .execute()
                .onFailure(throwable -> log.error("Error inserting to finnhub.calendar_ipo ", throwable))
                .onSuccess(rowSet ->
                  log.info("IPO updater inserted {} rows", rowSet.rowCount())
                );
            }
          );
      }
    );
    return succeededFuture();
  }
}
