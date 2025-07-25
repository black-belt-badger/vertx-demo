package bbb.vertx_demo.main.http_server.stock;

import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.templ.thymeleaf.ThymeleafTemplateEngine;
import lombok.extern.slf4j.Slf4j;

import static bbb.vertx_demo.main.http_server.HttpServerStarter.*;
import static bbb.vertx_demo.main.http_server.HttpServerStarter.FINNHUB_API_KEY;

@Slf4j
public enum StockFinancialsReported {

  ;

  public static Handler<RoutingContext> stockFinancialsReported(WebClient client, ThymeleafTemplateEngine engine) {
    return context -> {
      var symbol = context.pathParam("symbol");
      client
        .get(FINNHUB_PORT, FINNHUB_HOST, "/api/v1/stock/financials-reported?symbol=" + symbol)
        .putHeader(FINNHUB_HEADER, FINNHUB_API_KEY)
        .send()
        .onFailure(throwable -> log.error("error sending request", throwable))
        .onSuccess(response -> {
            var object = response.bodyAsJsonObject();
            engine
              .render(new JsonObject().put("object", object).put("symbol", symbol), "templates/stock/financials-reported.html")
              .onFailure(throwable -> log.error("error rendering template", throwable))
              .onSuccess(buffer ->
                context.response().putHeader("content-type", "text/html").end(buffer)
              );
          }
        );
    };
  }
}
