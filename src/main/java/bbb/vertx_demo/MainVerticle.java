package bbb.vertx_demo;

import io.vertx.config.ConfigRetriever;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.VerticleBase;
import io.vertx.core.json.JsonObject;
import io.vertx.core.net.ClientSSLOptions;
import io.vertx.ext.shell.ShellVerticle;
import io.vertx.ext.shell.command.CommandBuilder;
import io.vertx.ext.shell.command.CommandRegistry;
import io.vertx.jdbcclient.JDBCPool;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.pgclient.PgConnection;
import io.vertx.sqlclient.PoolOptions;
import lombok.extern.slf4j.Slf4j;
import software.amazon.jdbc.ds.AwsWrapperDataSource;

import javax.management.*;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicReference;

import static io.vertx.core.Future.failedFuture;
import static io.vertx.core.Future.succeededFuture;
import static io.vertx.pgclient.SslMode.ALLOW;
import static java.lang.String.format;
import static java.lang.System.getenv;
import static java.lang.management.ManagementFactory.getPlatformMBeanServer;
import static java.util.Optional.ofNullable;

@Slf4j
public final class MainVerticle extends VerticleBase {

  private static final String VERSION = ofNullable(getenv("VERSION")).orElse("unknown");

  @Override
  public Future<?> start() {
    var starting = config();
    log.info("Start config: {}", starting.encodePrettily());
    var configServer = starting.getJsonObject("config-server", new JsonObject());
    var configServerHost = configServer.getString("host", "localhost");
    log.info("Config server host: {}", configServerHost);
    var configServerPort = configServer.getInteger("port", 8887);
    log.info("Config server port: {}", configServerPort);
    var configServerPath = configServer.getString("path", "/conf.json");
    log.info("Config server path: {}", configServerPath);
    var configServerScanPeriodString = configServer.getString("scan-period", "PT30S");
    log.info("Config server scan period string: {}", configServerScanPeriodString);
    var configServerScanPeriod = Duration.parse(configServerScanPeriodString);
    log.info("Config server scan period: {}", configServerScanPeriod);
    var retriever =
      ConfigRetriever.create(vertx,
        new ConfigRetrieverOptions()
          .setScanPeriod(configServerScanPeriod.toMillis())
          .addStore(
            new ConfigStoreOptions()
              .setType("http")
              .setOptional(true)
              .setConfig(
                new JsonObject()
                  .put("host", configServerHost)
                  .put("port", configServerPort)
                  .put("path", configServerPath)
                  .put("headers",
                    new JsonObject()
                      .put("Accept", "application/json")
                  )
              )
          )
      );
    return retriever
      .getConfig().map(retrieved -> {
          log.info("Retrieved config: {}", retrieved.encodePrettily());
          var merged = retrieved.mergeIn(starting);
          log.info("Merged config: {}", merged.encodePrettily());
          var httpHost = merged.getString("http.host", "0.0.0.0");
          int httpPort = merged.getInteger("http.port", 8080);
          var telnetHost = merged.getString("telnet.host", "0.0.0.0");
          int telnetPort = merged.getInteger("telnet.port", 5000);
          var postgres = merged.getJsonObject("postgres", new JsonObject());
          var pgHost = postgres.getString("host", "localhost");
          int pgPort = postgres.getInteger("port", 5432);
          var pgDatabase = postgres.getString("database", "vertx_demo_database");
          var pgUser = postgres.getString("user", "vertx_demo_user");
          var pgPassword = postgres.getString("password", "vertx_demo_password");
          var configServerVersion =
            merged
              .getJsonObject("config-server", new JsonObject())
              .getString("version", "compiled default value");
          var configServerVersionRef = new AtomicReference<>(configServerVersion);
          retriever
            .setBeforeScanHandler(ignored -> {
                if (log.isDebugEnabled())
                  log.debug("About to scan config");
              }
            )
            .setConfigurationProcessor(config -> {
                if (log.isDebugEnabled())
                  log.debug("Processing config: {}", config.encodePrettily());
                return config;
              }
            )
            .listen(change -> {
                var previous = change.getPreviousConfiguration();
                var next = change.getNewConfiguration();
                log.info("Config changed from {} to {}", previous.encodePrettily(), next.encodePrettily());
                var newConfigServerVersion =
                  next
                    .getJsonObject("config-server", new JsonObject())
                    .getString("version");
                if (newConfigServerVersion != null)
                  configServerVersionRef.set(newConfigServerVersion);
              }
            );
          return vertx
            .createHttpServer()
            .requestHandler(request -> {
                if (log.isTraceEnabled())
                  log.trace("Received request: {}", request.uri());
                request
                  .response()
                  .putHeader("content-type", "text/plain")
                  .end(format("Hello from Vert.x Demo, version '%s', config '%s'!", VERSION, configServerVersionRef.get()));
              }
            )
            .listen(httpPort, httpHost)
            .onSuccess(httpServer -> log.info("HTTP server started on internal {}:{}", httpHost, httpPort))
            .onFailure(throwable -> log.error("HTTP server failed to start on internal {}:{}", httpHost, httpPort, throwable))
            .flatMap(ignored ->
              vertx.deployVerticle(
                ShellVerticle.class,
                new DeploymentOptions()
                  .setConfig(
                    new JsonObject()
                      .put("telnetOptions",
                        new JsonObject()
                          .put("host", telnetHost)
                          .put("port", telnetPort)
                      )
                  )
              )
            )
            .onSuccess(id -> log.info("Shell Verticle deployed on internal {}:{} with id {}", telnetHost, telnetPort, id))
            .onFailure(throwable -> log.error("Shell Verticle failed to deploy on internal {}:{}", telnetHost, telnetPort, throwable))
            .flatMap(ignored ->
              CommandRegistry
                .getShared(vertx)
                .registerCommand(
                  CommandBuilder
                    .command("print-config")
                    .processHandler(process ->
                      process
                        .write("config: ")
                        .write(starting.encodePrettily())
                        .write("\n")
                        .end()
                    )
                    .build(vertx)
                )
            )
            .onSuccess(command -> log.info("Registered command {}", command.name()))
            .onFailure(throwable -> log.error("Could not register command", throwable))
            .flatMap(ignored -> {
                var mbean = new Controller();
                var name = "bbb.vertx_demo:type=basic,name=vertx-demo";
                log.info("Registering MBean {}", name);
                try {
                  var objectName = new ObjectName(name);
                  var instance = getPlatformMBeanServer().registerMBean(mbean, objectName);
                  return succeededFuture(instance);
                } catch (MalformedObjectNameException |
                         InstanceAlreadyExistsException |
                         MBeanRegistrationException |
                         NotCompliantMBeanException e) {
                  return failedFuture(e);
                }
              }
            )
            .andThen(ignored -> {
                var options =
                  new PgConnectOptions()
                    .setPort(pgPort)
                    .setHost(pgHost)
                    .setSslMode(ALLOW)
                    .setSslOptions(
                      new ClientSSLOptions()
                        .setTrustAll(true)
                    )
                    .setDatabase(pgDatabase)
                    .setUser(pgUser)
                    .setPassword(pgPassword);
                log.info("PostgreSQL connection options {}", options.toJson());
                var poolOptions = new PoolOptions().setMaxSize(5);
                log.info("PostgreSQL pool options {}", poolOptions.toJson());
                PgConnection.connect(vertx, options)
                  .onSuccess(connection -> {
                      int processId = connection.processId();
                      int secretKey = connection.secretKey();
                      log.info("Connection to PostgreSQL succeeded, process ID {}, secret key {}", processId, secretKey);
                      connection
                        .query("SELECT * FROM information_schema.enabled_roles")
                        .execute()
                        .onSuccess(rows -> {
                            log.info("PostgreSQL connection succeeded and returned {} row(s)", rows.size());
                            rows.forEach(row ->
                              log.info("PostgreSQL connection row {}", row.toJson())
                            );
                          }
                        )
                        .onFailure(throwable -> log.error("PostgreSQL connection failed", throwable));
                    }
                  )
                  .onFailure(throwable -> log.error("Connection to PostgreSQL failed", throwable));
              }
            )
            .andThen(ignored -> {
                var source = new AwsWrapperDataSource();
                source.setJdbcUrl("jdbc:aws-wrapper:postgresql://" + pgHost + ":5432/postgres");
                source.setUser(pgUser);
                source.setPassword(pgPassword);
                var options = new PoolOptions().setMaxSize(16);
                var pool = JDBCPool.pool(vertx, source, options);
                log.info("JDBC connection data source {}", source);
                pool
                  .query("SELECT * FROM information_schema.enabled_roles")
                  .execute()
                  .onSuccess(rows -> {
                      log.info("JDBC connection succeeded and returned {} row(s)", rows.size());
                      rows.forEach(row ->
                        log.info("JDBC connection row {}", row.toJson())
                      );
                    }
                  )
                  .onFailure(throwable -> log.error("JDBC connection failed", throwable));
              }
            )
            ;
        }
      );
  }
}
