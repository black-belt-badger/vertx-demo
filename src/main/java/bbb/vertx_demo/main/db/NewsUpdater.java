package bbb.vertx_demo.main.db;

import io.vertx.core.Future;
import io.vertx.core.VerticleBase;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;
import io.vertx.pgclient.PgConnection;
import io.vertx.sqlclient.Tuple;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;

import static bbb.vertx_demo.main.FinnHubApi.NEWS_GENERAL;
import static bbb.vertx_demo.main.http_server.HttpServerStarter.*;
import static io.vertx.core.Future.succeededFuture;
import static java.lang.String.format;

@Slf4j
@RequiredArgsConstructor
public final class NewsUpdater extends VerticleBase {

  private final WebClient client;
  private final PgConnection pgConnection;
  private final String name;
  private final Duration delay;
  private final String table;
  private final String view;

  @Override
  public Future<Long> start() throws Exception {
    return succeededFuture(
      vertx.setPeriodic(delay.toMillis(), handler -> {
          log.info("News/{} updater started, handler {}", name, handler);
          pgConnection
            .preparedQuery("SELECT max(id) FROM " + table)
            .execute()
            .onFailure(error ->
              log.error("Error selecting max ID from {}", table, error)
            )
            .map(rowSet -> {
                var row = rowSet.iterator().next();
                return row.getLong("max");
              }
            )
            .flatMap(maxId -> {
                log.info("Max ID in {} is {}", table, maxId);
                var uri = NEWS_GENERAL + "&minId=" + maxId;
                return client
                  .get(FINNHUB_PORT, FINNHUB_HOST, uri)
                  .putHeader(FINNHUB_HEADER, FINNHUB_API_KEY)
                  .send()
                  .onFailure(throwable ->
                    log.error("Error getting from {}", uri, throwable)
                  )
                  .onSuccess(response -> {
                      var array = response.bodyAsJsonArray();
                      log.info("News/{} got array with {} elements", name, array.size());
                      if (array.isEmpty())
                        return;
                      var values = array.stream()
                        .map(element -> (JsonObject) element)
                        .map(obj -> {
                            var category = obj.getString("category");
                            var datetime = obj.getLong("datetime");
                            var headline = obj.getString("headline");
                            var id = obj.getLong("id");
                            var image = obj.getString("image");
                            var related = obj.getString("related");
                            var source = obj.getString("source");
                            var summary = obj.getString("summary");
                            var url = obj.getString("url");
                            return Tuple.of(category, datetime, headline, id, image, related, source, summary, url);
                          }
                        ).toList();
                      var insert =
                        format("""
                            INSERT INTO %s (category, datetime, headline, id, image, related, source, summary, url)
                            VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9)
                            """,
                          table
                        );
                      pgConnection
                        .preparedQuery(insert)
                        .executeBatch(values)
                        .onFailure(throwable -> log.error("Error inserting to {}", table, throwable))
                        .onSuccess(rowSet ->
                          pgConnection
                            .preparedQuery("REFRESH MATERIALIZED VIEW " + view)
                            .execute()
                            .onFailure(throwable -> log.error("Error refreshing {}", view, throwable))
                            .onSuccess(result -> log.info("Refreshed {}", view))
                        );
                    }
                  );
              }
            );
        }
      )
    );
  }
}
