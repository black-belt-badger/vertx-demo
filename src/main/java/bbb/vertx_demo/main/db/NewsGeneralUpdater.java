package bbb.vertx_demo.main.db;

import io.vertx.core.Future;
import io.vertx.core.VerticleBase;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;
import io.vertx.pgclient.PgConnection;
import io.vertx.sqlclient.Tuple;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import static bbb.vertx_demo.main.FinnHubApi.NEWS_GENERAL;
import static bbb.vertx_demo.main.http_server.HttpServerStarter.*;
import static io.vertx.core.Future.succeededFuture;

@Slf4j
@RequiredArgsConstructor
public final class NewsGeneralUpdater extends VerticleBase {

  private final WebClient client;
  private final PgConnection pgConnection;

  @Override
  public Future<Long> start() throws Exception {
    long timerId =
      vertx.setPeriodic(0, 10_000, handler -> {
          log.info("News general updater started, handler {}", handler);
          pgConnection
            .preparedQuery("SELECT max(id) FROM finnhub.news_general")
            .execute()
            .onFailure(error ->
              log.error("Error selecting max ID from finnhub.news_general ", error)
            )
            .map(rowSet -> {
                var row = rowSet.iterator().next();
                return row.getLong("max");
              }
            )
            .flatMap(maxId -> {
                log.info("Max ID in finnhub.news_general is {}", maxId);
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
                      log.info("News general got array with {} elements", array.size());
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
                      var insert = """
                        INSERT INTO finnhub.news_general
                          (category, datetime, headline, id, image, related, source, summary, url)
                        VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9)
                        ON CONFLICT ON CONSTRAINT unique_id DO NOTHING
                        """;
                      pgConnection
                        .preparedQuery(insert)
                        .executeBatch(values)
                        .onFailure(throwable -> log.error("Error inserting to finnhub.news_general ", throwable))
                        .onSuccess(rowSet ->
                          pgConnection
                            .preparedQuery("REFRESH MATERIALIZED VIEW finnhub.news_general_view")
                            .execute()
                            .onFailure(throwable -> log.error("Error refreshing finnhub.news_general_view", throwable))
                            .onSuccess(result -> log.info("Refreshed finnhub.news_general_view"))
                        );
                    }
                  );
              }
            );
        }
      );
    return succeededFuture(timerId);
  }
}
