package bbb.vertx_demo.main;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.healthchecks.Status;
import io.vertx.ext.shell.command.Command;
import io.vertx.ext.shell.command.CommandBuilder;
import io.vertx.ext.shell.command.CommandRegistry;
import io.vertx.ext.web.healthchecks.HealthCheckHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public enum ShellCommandHelper {

  ;

  private static final String COMMAND_REGISTRATION = "command-registration";

  public static Future<Command> registerCommandPrintConfig
    (
      Vertx vertx,
      HealthCheckHandler checks,
      JsonObject config
    ) {
    var printConfig = CommandBuilder
      .command("print-config")
      .processHandler(process ->
        process
          .write("config: ")
          .write(config.encodePrettily())
          .write("\n")
          .end()
      )
      .build(vertx);
    return CommandRegistry
      .getShared(vertx)
      .registerCommand(printConfig)
      .onSuccess(command -> {
          log.info("Registering command succeeded {}", command.name());
          checks.register(COMMAND_REGISTRATION, Promise::succeed);
        }
      )
      .onFailure(throwable -> {
          log.error("Registering command failed", throwable);
          checks.register(COMMAND_REGISTRATION, promise ->
            promise.complete(Status.KO(), throwable)
          );
        }
      );
  }
}
