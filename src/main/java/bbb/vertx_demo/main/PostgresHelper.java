package bbb.vertx_demo.main;

import io.vertx.core.Future;
import io.vertx.core.Handler;
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
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.output.MigrateResult;

import static io.vertx.core.net.ClientSSLOptions.DEFAULT_TRUST_ALL;
import static io.vertx.ext.healthchecks.Status.KO;
import static io.vertx.pgclient.PgConnectOptions.DEFAULT_SSLMODE;

@Slf4j
public enum PostgresHelper {

  ;

  public static PgConnectOptions pgConnectOptions(JsonObject config) {
    var host = config.getString("host", "localhost");
    int port = config.getInteger("port", 5432);
    var database = config.getString("database", "vertx_demo_database");
    var user = config.getString("user", "vertx_demo_user");
    var password = config.getString("password", "vertx_demo_password");
    var sslModeString = config.getString("ssl-mode", DEFAULT_SSLMODE.toString());
    var sslMode = SslMode.valueOf(sslModeString.toUpperCase());
    var trustAll = config.getBoolean("trust-all", DEFAULT_TRUST_ALL);
    var sslOptions = new ClientSSLOptions().setTrustAll(trustAll);
    return new PgConnectOptions()
      .setPort(port)
      .setHost(host)
      .setSslMode(sslMode)
      .setSslOptions(sslOptions)
      .setDatabase(database)
      .setUser(user)
      .setPassword(password);
  }

  private static final String POSTGRES_CONNECTION = "postgres-connection";

  public static Handler<PgConnection> postgresConnectionSuccess(HealthCheckHandler checks) {
    return pgConnection -> {
      log.info("Connection to Postgres succeeded, process ID {}, secret key {}", pgConnection.processId(), pgConnection.secretKey());
      checks.register(POSTGRES_CONNECTION, Promise::complete);
    };
  }

  public static Handler<Throwable> postgresConnectionFailure(HealthCheckHandler checks) {
    return throwable -> {
      log.error("Failed to connect to Postgres server", throwable);
      checks.register(POSTGRES_CONNECTION, promise -> promise.complete(KO(), throwable));
    };
  }

  public static MigrateResult migratePgDatabase(JsonObject config) {
    var host = config.getString("host", "localhost");
    int port = config.getInteger("port", 5432);
    var database = config.getString("database", "vertx_demo_database");
    var url = "jdbc:postgresql://" + host + ":" + port + "/" + database;
    var user = config.getString("user", "vertx_demo_user");
    var password = config.getString("password", "vertx_demo_password");
    return Flyway
      .configure()
      .dataSource(url, user, password)
      .load()
      .migrate();
  }

  private static final String POSTGRES_MIGRATION = "postgres-migration";

  public static Handler<MigrateResult> postgresMigrationSuccess(HealthCheckHandler checks) {
    return migrateResult -> {
      log.info("Migration of Postgres DB succeeded, success? {}, warnings: {}", migrateResult.success, migrateResult.warnings);
      checks.register(POSTGRES_MIGRATION, Promise::complete);
    };
  }

  public static Handler<Throwable> postgresMigrationFailure(HealthCheckHandler checks) {
    return throwable -> {
      log.error("Failed to migrate Postgres DB", throwable);
      checks.register(POSTGRES_MIGRATION, promise -> promise.complete(KO(), throwable));
    };
  }

  private static final String POSTGRES_SELECT_QUERY_EXECUTION = "postgres-select-query-execution";
  private static final String POSTGRES_LISTEN_QUERY_EXECUTION = "postgres-listen-query-execution";

  @Deprecated
  public static Future<PgConnection> connectToPostgres
    (
      Vertx vertx,
      HealthCheckHandler checks,
      JsonObject postgres
    ) {
    var host = postgres.getString("host", "localhost");
    int port = postgres.getInteger("port", 5432);
    var database = postgres.getString("database", "vertx_demo_database");
    var user = postgres.getString("user", "vertx_demo_user");
    var password = postgres.getString("password", "vertx_demo_password");
    var sslModeString = postgres.getString("ssl-mode", DEFAULT_SSLMODE.toString());
    var sslMode = SslMode.valueOf(sslModeString.toUpperCase());
    var trustAll = postgres.getBoolean("trust-all", DEFAULT_TRUST_ALL);
    var sslOptions = new ClientSSLOptions().setTrustAll(trustAll);
    var options = new PgConnectOptions()
      .setPort(port)
      .setHost(host)
      .setSslMode(sslMode)
      .setSslOptions(sslOptions)
      .setDatabase(database)
      .setUser(user)
      .setPassword(password);
    log.info("Postgres connection options {}", options.toJson());
    var poolOptions = new PoolOptions().setMaxSize(5);
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
