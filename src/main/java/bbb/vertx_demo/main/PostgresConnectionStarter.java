package bbb.vertx_demo.main;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.core.net.ClientSSLOptions;
import io.vertx.ext.web.healthchecks.HealthCheckHandler;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.pgclient.PgConnection;
import io.vertx.pgclient.SslMode;
import io.vertx.sqlclient.PoolOptions;
import lombok.extern.slf4j.Slf4j;

import static io.vertx.core.net.ClientSSLOptions.DEFAULT_TRUST_ALL;
import static io.vertx.ext.healthchecks.Status.KO;
import static io.vertx.pgclient.PgConnectOptions.DEFAULT_SSLMODE;

@Slf4j
public enum PostgresConnectionStarter {

  ;

  private static final String POSTGRES_CONNECTION = "postgres-connection";
  private static final String POSTGRES_SELECT_QUERY_EXECUTION = "postgres-select-query-execution";
  private static final String POSTGRES_LISTEN_QUERY_EXECUTION = "postgres-listen-query-execution";

  public static Future<PgConnection> connectToPostgres
    (
      Vertx vertx,
      HealthCheckHandler checks,
      JsonObject postgres
    ) {
    var host =
      postgres.getString("host", "localhost");
    int port =
      postgres.getInteger("port", 5432);
    var database =
      postgres.getString("database", "vertx_demo_database");
    var user =
      postgres.getString("user", "vertx_demo_user");
    var password =
      postgres.getString("password", "vertx_demo_password");
    var sslModeString =
      postgres.getString("ssl-mode", DEFAULT_SSLMODE.toString());
    var sslMode =
      SslMode.valueOf(sslModeString.toUpperCase());
    var trustAll =
      postgres.getBoolean("trust-all", DEFAULT_TRUST_ALL);
    var sslOptions =
      new ClientSSLOptions()
        .setTrustAll(trustAll);
    var options =
      new PgConnectOptions()
        .setPort(port)
        .setHost(host)
        .setSslMode(sslMode)
        .setSslOptions(sslOptions)
        .setDatabase(database)
        .setUser(user)
        .setPassword(password);
    log.info("Postgres connection options {}", options.toJson());
    var poolOptions =
      new PoolOptions()
        .setMaxSize(5);
    log.info("Postgres pool options {}", poolOptions.toJson());
    return PgConnection
      .connect(vertx, options)
      .onSuccess(connection -> {
          int processId = connection.processId();
          int secretKey = connection.secretKey();
          checks.register(POSTGRES_CONNECTION, Promise::complete);
          log.info("Connection to Postgres succeeded, process ID {}, secret key {}", processId, secretKey);
          connection
            .query("SELECT * FROM users")
            .execute()
            .onSuccess(rows -> {
                checks.register(POSTGRES_SELECT_QUERY_EXECUTION, Promise::complete);
                log.info("Postgres SELECT query succeeded and returned {} row(s)", rows.size());
                rows
                  .forEach(row ->
                    log.info("Postgres connection row {}", row.toJson())
                  );
              }
            )
            .onFailure(throwable -> {
                checks.register(POSTGRES_SELECT_QUERY_EXECUTION, promise ->
                  promise.complete(KO(), throwable)
                );
                log.error("Postgres connection failed", throwable);
              }
            );
          connection
            .notificationHandler(notification ->
              log.info("Postgres notification {}", notification.toJson())
            )
            .query("LISTEN my_channel")
            .execute()
            .onSuccess(rows -> {
                checks.register(POSTGRES_LISTEN_QUERY_EXECUTION, Promise::complete);
                log.info("Postgres LISTEN query succeeded and returned {} row(s)", rows.size());
                rows
                  .forEach(row ->
                    log.info("Postgres query row {}", row.toJson())
                  );
              }
            )
            .onFailure(throwable -> {
                checks.register(POSTGRES_LISTEN_QUERY_EXECUTION, promise ->
                  promise.complete(KO(), throwable)
                );
                log.error("Postgres query failed", throwable);
              }
            );
          connection.noticeHandler(notice ->
            log.info("Postgres notice {}", notice.toJson())
          );
        }
      )
      .onFailure(throwable -> {
          checks.register(POSTGRES_CONNECTION, promise ->
            promise.complete(KO(), throwable)
          );
          log.error("Connection to Postgres failed", throwable);
        }
      );
  }
}
