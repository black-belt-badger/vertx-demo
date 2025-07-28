package bbb.vertx_demo.main;

import io.vertx.amqp.AmqpClient;
import io.vertx.amqp.AmqpClientOptions;
import io.vertx.amqp.AmqpMessage;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.healthchecks.HealthCheckHandler;
import lombok.extern.slf4j.Slf4j;

import static io.vertx.ext.healthchecks.Status.KO;
import static java.lang.Integer.MAX_VALUE;

@Slf4j
public enum AmqpHelper {

  ;

  private static final String AMQP_SERVER_CONNECTION = "amqp-server-connection";
  private static final String SERVER_RECEIVER_CREATION = "server-queue-receiver-creation";
  private static final String SERVER_SENDER_CREATION = "server-queue-sender-creation";
  private static final String CLIENT_RECEIVER_CREATION = "client-queue-receiver-creation";
  private static final String CLIENT_SENDER_CREATION = "client-queue-sender-creation";
  private static final String SERVER_MESSAGE_ACKNOWLEDGEMENT = "server-message-acknowledgement";
  private static final String CLIENT_MESSAGE_ACKNOWLEDGEMENT = "client-message-acknowledgement";

  public static Future<?> deployReceiverAndSender
    (
      Vertx vertx,
      HealthCheckHandler checks,
      JsonObject config
    ) {
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
        .onSuccess(conn -> {
            checks.register(AMQP_SERVER_CONNECTION, Promise::succeed);
            log.info("AMQP server connection established");
          }
        )
        .onFailure(throwable -> {
            checks.register(AMQP_SERVER_CONNECTION, promise ->
              promise.complete(KO(), throwable)
            );
            log.error("Failed to connect to AMQP server", throwable);
          }
        )
        .compose(connection ->
          connection
            .createReceiver(serverQueue)
            .onSuccess(receiver -> {
                checks.register(SERVER_RECEIVER_CREATION, Promise::succeed);
                log.info("Server receiver created");
              }
            )
            .onFailure(throwable -> {
                checks.register(SERVER_RECEIVER_CREATION, promise ->
                  promise.complete(KO(), throwable)
                );
                log.error("Server receiver creation failed", throwable);
              }
            )
            .flatMap(
              serverReceiver ->
                connection.createSender(serverQueue)
                  .onSuccess(receiver -> {
                      checks.register(SERVER_SENDER_CREATION, Promise::succeed);
                      log.info("Server sender created");
                    }
                  )
                  .onFailure(throwable -> {
                      checks.register(SERVER_SENDER_CREATION, promise ->
                        promise.complete(KO(), throwable)
                      );
                      log.error("Server sender creation failed", throwable);
                    }
                  )
                  .flatMap(
                    serverSender -> connection
                      .createReceiver(clientQueue)
                      .onSuccess(receiver -> {
                          checks.register(CLIENT_RECEIVER_CREATION, Promise::succeed);
                          log.info("Client receiver created");
                        }
                      )
                      .onFailure(throwable -> {
                          checks.register(CLIENT_RECEIVER_CREATION, promise ->
                            promise.complete(KO(), throwable)
                          );
                          log.error("Client receiver creation failed", throwable);
                        }
                      )
                      .flatMap(clientReceiver ->
                        connection.createSender(clientQueue)
                          .onSuccess(receiver -> {
                              checks.register(CLIENT_SENDER_CREATION, Promise::succeed);
                              log.info("Client sender created");
                            }
                          )
                          .onFailure(throwable -> {
                              checks.register(CLIENT_SENDER_CREATION, promise ->
                                promise.complete(KO(), throwable)
                              );
                              log.error("Client sender creation failed", throwable);
                            }
                          )
                          .map(
                            clientSender -> {
                              serverReceiver.handler(fromClient -> {
                                  log.info("Server received {}", fromClient.bodyAsString());
                                  vertx.setTimer(serverDelay, handler -> {
                                      var toClient = AmqpMessage.create().withBody("pong").build();
                                      clientSender
                                        .sendWithAck(toClient)
                                        .onSuccess(receiver -> {
                                            checks.register(CLIENT_MESSAGE_ACKNOWLEDGEMENT, Promise::succeed);
                                            log.trace("Client message acknowledged");
                                          }
                                        )
                                        .onFailure(throwable -> {
                                            checks.register(CLIENT_MESSAGE_ACKNOWLEDGEMENT, promise ->
                                              promise.complete(KO(), throwable)
                                            );
                                            log.error("Client message not acknowledged", throwable);
                                          }
                                        );
                                    }
                                  );
                                }
                              );
                              clientReceiver.handler(fromServer -> {
                                  log.info("Client received {}", fromServer.bodyAsString());
                                  vertx.setTimer(clientDelay, handler -> {
                                      var toServer = AmqpMessage.create().withBody("ping").build();
                                      serverSender
                                        .sendWithAck(toServer)
                                        .onSuccess(receiver -> {
                                            checks.register(SERVER_MESSAGE_ACKNOWLEDGEMENT, Promise::succeed);
                                            log.trace("Server message acknowledged");
                                          }
                                        )
                                        .onFailure(throwable -> {
                                            checks.register(SERVER_MESSAGE_ACKNOWLEDGEMENT, promise ->
                                              promise.complete(KO(), throwable)
                                            );
                                            log.error("Server message not acknowledged", throwable);
                                          }
                                        );
                                    }
                                  );
                                }
                              );
                              var toServer = AmqpMessage.create().withBody("ping").build();
                              return serverSender
                                .sendWithAck(toServer)
                                .onSuccess(receiver -> {
                                    checks.register(SERVER_MESSAGE_ACKNOWLEDGEMENT, Promise::succeed);
                                    log.trace("Server message acknowledged");
                                  }
                                )
                                .onFailure(throwable -> {
                                    checks.register(SERVER_MESSAGE_ACKNOWLEDGEMENT, promise ->
                                      promise.complete(KO(), throwable)
                                    );
                                    log.error("Server message not acknowledged", throwable);
                                  }
                                );
                            }
                          )
                      )
                  )
            )
        );
  }
}
