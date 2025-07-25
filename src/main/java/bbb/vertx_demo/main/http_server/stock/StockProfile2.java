package bbb.vertx_demo.main.http_server.stock;

import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.common.template.TemplateEngine;
import lombok.extern.slf4j.Slf4j;

import static bbb.vertx_demo.main.http_server.HttpServerStarter.*;
import static bbb.vertx_demo.main.http_server.HttpServerStarter.FINNHUB_API_KEY;

@Slf4j
public enum StockProfile2 {

  ;

  public static Handler<RoutingContext> stockProfile2(WebClient client, TemplateEngine engine) {
    return context -> {
      var symbol = context.pathParam("symbol");
      client
        .get(FINNHUB_PORT, FINNHUB_HOST, "/api/v1/stock/profile2?symbol=" + symbol)
        .putHeader(FINNHUB_HEADER, FINNHUB_API_KEY)
        .send()
        .onFailure(throwable -> log.error("error sending request", throwable))
        .onSuccess(response -> {
            var object = response.bodyAsJsonObject();
            engine
              .render(new JsonObject().put("profile", object), "templates/stock/profile2.html")
              .onFailure(throwable -> log.error("error rendering template", throwable))
              .onSuccess(buffer ->
                context.response().putHeader("content-type", "text/html").end(buffer)
              );
          }
        );
    };
  }
}
