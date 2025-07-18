package bbb.vertx_demo.main;

import io.vertx.amqp.AmqpClient;
import io.vertx.amqp.AmqpClientOptions;
import io.vertx.amqp.AmqpMessage;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeoutException;

import static java.lang.Integer.MAX_VALUE;

@Slf4j
public enum AmqpDeployer {

  ;

  public static Future<?> deployReceiverAndSender(Vertx vertx, JsonObject config) throws TimeoutException {
    var host = config.getString("host", "localhost");
    var port = config.getInteger("port", 5672);
    var username = config.getString("username", "admin");
    var password = config.getString("password", "admin");
    var reconnectAttempts = config.getInteger("reconnect-attempts", MAX_VALUE);
    var reconnectInterval = config.getInteger("reconnect-interval", 100);
    var options =
      new AmqpClientOptions()
        .setHost(host)
        .setPort(port)
        .setUsername(username)
        .setPassword(password)
        .setReconnectAttempts(reconnectAttempts)
        .setReconnectInterval(reconnectInterval);
    var amqp = AmqpClient.create(vertx, options);
    var server = config.getJsonObject("server", new JsonObject());
    var serverQueue = server.getString("queue", "server-queue");
    var serverDelay = server.getInteger("delay", 1_000);
    var client = config.getJsonObject("client", new JsonObject());
    var clientQueue = client.getString("queue", "client-queue");
    var clientDelay = client.getInteger("delay", 1_000);
    return
      amqp
        .connect()
        .onFailure(throwable ->
          log.error("Failed to connect to server", throwable))
        .compose(connection ->
          connection
            .createReceiver(serverQueue).flatMap(
              serverReceiver ->
                connection.createSender(serverQueue).flatMap(
                  serverSender -> connection
                    .createReceiver(clientQueue).flatMap(clientReceiver ->
                      connection.createSender(clientQueue).map(
                        clientSender -> {
                          serverReceiver.handler(fromClient -> {
                              log.info("Server received {}", fromClient.bodyAsString());
                              vertx.setTimer(serverDelay, handler -> {
                                  var toClient = AmqpMessage.create().withBody("pong").build();
                                  clientSender.send(toClient);
                                }
                              );
                            }
                          );
                          clientReceiver.handler(fromServer -> {
                              log.info("Client received {}", fromServer.bodyAsString());
                              vertx.setTimer(clientDelay, handler -> {
                                  var toServer = AmqpMessage.create().withBody("ping").build();
                                  serverSender.send(toServer);
                                }
                              );
                            }
                          );
                          var toServer = AmqpMessage.create().withBody("ping").build();
                          return serverSender.send(toServer);
                        }
                      )
                    )
                )
            )
        );
  }
}
