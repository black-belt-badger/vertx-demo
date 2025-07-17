package bbb.vertx_demo.main;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.shell.command.Command;
import io.vertx.ext.shell.command.CommandBuilder;
import io.vertx.ext.shell.command.CommandRegistry;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public enum CommandRegistrator {

  ;

  public static Future<Command> registerCommand
    (
      Vertx vertx,
      JsonObject config
    ) {
    return CommandRegistry
      .getShared(vertx)
      .registerCommand(
        CommandBuilder
          .command("print-config")
          .processHandler(process ->
            process
              .write("config: ")
              .write(config.encodePrettily())
              .write("\n")
              .end()
          )
          .build(vertx)
      )
      .onSuccess(command ->
        log.info("Registering command succeeded {}", command.name())
      )
      .onFailure(throwable ->
        log.error("Registering command failed", throwable)
      );
  }
}
