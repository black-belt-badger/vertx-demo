package bbb.vertx_demo.main.http_server.forex;

import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.templ.thymeleaf.ThymeleafTemplateEngine;
import lombok.extern.slf4j.Slf4j;

import static bbb.vertx_demo.main.http_server.HttpServerStarter.*;

@Slf4j
public enum ForexHandlers {

  ;

  public static Handler<RoutingContext> forexSymbol(WebClient client, ThymeleafTemplateEngine engine) {
    return context -> {
      var exchange = context.pathParam("exchange");
      client
        .get(FINNHUB_PORT, FINNHUB_HOST, "/api/v1/forex/symbol?exchange=" + exchange)
        .putHeader(FINNHUB_HEADER, FINNHUB_API_KEY)
        .send()
        .onFailure(throwable -> log.error("error sending request", throwable))
        .onSuccess(response -> {
            var array = response.bodyAsJsonArray();
            engine
              .render(new JsonObject().put("symbols", array), "templates/forex/symbol.html")
              .onFailure(throwable -> log.error("error rendering template", throwable))
              .onSuccess(buffer ->
                context.response().putHeader("content-type", "text/html").end(buffer)
              );
          }
        );
    };
  }

  public static Handler<RoutingContext> forexExchange(WebClient client, ThymeleafTemplateEngine engine) {
    return context ->
      client
        .get(FINNHUB_PORT, FINNHUB_HOST, "/api/v1/forex/exchange")
        .putHeader(FINNHUB_HEADER, FINNHUB_API_KEY)
        .send()
        .onFailure(throwable -> log.error("error sending request", throwable))
        .onSuccess(response -> {
            var array = response.bodyAsJsonArray();
            engine
              .render(new JsonObject().put("exchanges", array), "templates/forex/exchange.html")
              .onFailure(throwable -> log.error("error rendering template", throwable))
              .onSuccess(buffer ->
                context.response().putHeader("content-type", "text/html").end(buffer)
              );
          }
        );
  }
}
