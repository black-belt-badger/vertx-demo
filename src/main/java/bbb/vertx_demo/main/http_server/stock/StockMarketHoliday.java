package bbb.vertx_demo.main.http_server.stock;

import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.templ.thymeleaf.ThymeleafTemplateEngine;
import lombok.extern.slf4j.Slf4j;

import static bbb.vertx_demo.main.http_server.HttpServerStarter.*;

@Slf4j
public enum StockMarketHoliday {

  ;

  public static Handler<RoutingContext> stockMarketHoliday(WebClient client, ThymeleafTemplateEngine engine) {
    return context -> {
      var exchange = context.pathParam("exchange");
      client
        .get(FINNHUB_PORT, FINNHUB_HOST, "/api/v1/stock/market-holiday?exchange=" + exchange)
        .putHeader(FINNHUB_HEADER, FINNHUB_API_KEY)
        .send()
        .onFailure(throwable -> log.error("error sending request", throwable))
        .onSuccess(response -> {
            var object = response.bodyAsJsonObject();
            engine
              .render(new JsonObject().put("object", object).put("exchange", exchange), "templates/stock/market-holiday.html")
              .onFailure(throwable -> log.error("error rendering template", throwable))
              .onSuccess(buffer ->
                context.response().putHeader("content-type", "text/html").end(buffer)
              );
          }
        );
    };
  }
}
