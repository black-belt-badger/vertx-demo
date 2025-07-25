package bbb.vertx_demo.main.http_server;

import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.templ.thymeleaf.ThymeleafTemplateEngine;
import lombok.extern.slf4j.Slf4j;

import static bbb.vertx_demo.main.http_server.HttpServerStarter.*;
import static bbb.vertx_demo.main.http_server.HttpServerStarter.FINNHUB_API_KEY;

@Slf4j
public enum News {

  ;

  static Handler<RoutingContext> news(WebClient client, ThymeleafTemplateEngine engine) {
    return context -> {
      var category = context.pathParam("category");
      client
        .get(FINNHUB_PORT, FINNHUB_HOST, "/api/v1/news?category=" + category)
        .putHeader(FINNHUB_HEADER, FINNHUB_API_KEY)
        .send()
        .onFailure(throwable -> log.error("error sending request", throwable))
        .onSuccess(response -> {
            var array = response.bodyAsJsonArray();
            engine
              .render(new JsonObject().put("news", array).put("caption", category + " news"), "templates/news.html")
              .onFailure(throwable -> log.error("error rendering template", throwable))
              .onSuccess(buffer ->
                context.response().putHeader("content-type", "text/html").end(buffer)
              );
          }
        );
    };
  }
}
