package bbb.vertx_demo.main.http_server;

import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.templ.thymeleaf.ThymeleafTemplateEngine;
import lombok.extern.slf4j.Slf4j;

import static bbb.vertx_demo.main.http_server.HttpServerStarter.*;
import static java.lang.System.getenv;
import static java.util.Optional.ofNullable;

@Slf4j
public enum Handlers {

  ;

  private static final String VERSION = ofNullable(getenv("VERSION")).orElse("unknown");

  static Handler<RoutingContext> mergerNews(WebClient client, ThymeleafTemplateEngine engine) {
    return context ->
      client
        .get(FINNHUB_PORT, FINNHUB_HOST, "/api/v1/news?category=merger")
        .putHeader(FINNHUB_HEADER, FINNHUB_API_KEY)
        .send()
        .onFailure(throwable -> log.error("error sending request", throwable))
        .onSuccess(response -> {
            var array = response.bodyAsJsonArray();
            engine
              .render(new JsonObject().put("news", array).put("caption", "Merger news"), "templates/news.html")
              .onFailure(throwable -> log.error("error rendering template", throwable))
              .onSuccess(buffer ->
                context.response().putHeader("content-type", "text/html").end(buffer)
              );
          }
        );
  }

  static Handler<RoutingContext> cryptoNews(WebClient client, ThymeleafTemplateEngine engine) {
    return context ->
      client
        .get(FINNHUB_PORT, FINNHUB_HOST, "/api/v1/news?category=crypto")
        .putHeader(FINNHUB_HEADER, FINNHUB_API_KEY)
        .send()
        .onFailure(throwable -> log.error("error sending request", throwable))
        .onSuccess(response -> {
            var array = response.bodyAsJsonArray();
            engine
              .render(new JsonObject().put("news", array).put("caption", "Crypto news"), "templates/news.html")
              .onFailure(throwable -> log.error("error rendering template", throwable))
              .onSuccess(buffer ->
                context.response().putHeader("content-type", "text/html").end(buffer)
              );
          }
        );
  }

  static Handler<RoutingContext> forexNews(WebClient client, ThymeleafTemplateEngine engine) {
    return context ->
      client
        .get(FINNHUB_PORT, FINNHUB_HOST, "/api/v1/news?category=forex")
        .putHeader(FINNHUB_HEADER, FINNHUB_API_KEY)
        .send()
        .onFailure(throwable -> log.error("error sending request", throwable))
        .onSuccess(response -> {
            var array = response.bodyAsJsonArray();
            engine
              .render(new JsonObject().put("news", array).put("caption", "Forex news"), "templates/news.html")
              .onFailure(throwable -> log.error("error rendering template", throwable))
              .onSuccess(buffer ->
                context.response().putHeader("content-type", "text/html").end(buffer)
              );
          }
        );
  }

  static Handler<RoutingContext> generalNews(WebClient client, ThymeleafTemplateEngine engine) {
    return context ->
      client
        .get(FINNHUB_PORT, FINNHUB_HOST, "/api/v1/news?category=general")
        .putHeader(FINNHUB_HEADER, FINNHUB_API_KEY)
        .send()
        .onFailure(throwable -> log.error("error sending request", throwable))
        .onSuccess(response -> {
            var array = response.bodyAsJsonArray();
            engine
              .render(new JsonObject().put("news", array).put("caption", "General news"), "templates/news.html")
              .onFailure(throwable -> log.error("error rendering template", throwable))
              .onSuccess(buffer ->
                context.response().putHeader("content-type", "text/html").end(buffer)
              );
          }
        );
  }

  static Handler<RoutingContext> ipoCalendar(WebClient client, ThymeleafTemplateEngine engine) {
    return context ->
      client
        .get(FINNHUB_PORT, FINNHUB_HOST, "/api/v1/calendar/ipo")
        .putHeader(FINNHUB_HEADER, FINNHUB_API_KEY)
        .send()
        .onFailure(throwable -> log.error("error sending request", throwable))
        .onSuccess(response -> {
            var object = response.bodyAsJsonObject();
            engine
              .render(new JsonObject().put("object", object), "templates/calendar-ipo.html")
              .onFailure(throwable -> log.error("error rendering template", throwable))
              .onSuccess(buffer ->
                context.response().putHeader("content-type", "text/html").end(buffer)
              );
          }
        );
  }

  static Handler<RoutingContext> fdaAdvisoryCommiteeCalendar(WebClient client, ThymeleafTemplateEngine engine) {
    return context ->
      client
        .get(FINNHUB_PORT, FINNHUB_HOST, "/api/v1/fda-advisory-committee-calendar")
        .putHeader(FINNHUB_HEADER, FINNHUB_API_KEY)
        .send()
        .onFailure(throwable -> log.error("error sending request", throwable))
        .onSuccess(response -> {
            var array = response.bodyAsJsonArray();
            engine
              .render(new JsonObject().put("entries", array), "templates/fda-advisory-committee-calendar.html")
              .onFailure(throwable -> log.error("error rendering template", throwable))
              .onSuccess(buffer ->
                context.response().putHeader("content-type", "text/html").end(buffer)
              );
          }
        );
  }

  static Handler<RoutingContext> countries(WebClient client, ThymeleafTemplateEngine engine) {
    return context ->
      client
        .get(FINNHUB_PORT, FINNHUB_HOST, "/api/v1/country")
        .putHeader(FINNHUB_HEADER, FINNHUB_API_KEY)
        .send()
        .onFailure(throwable -> log.error("error sending request", throwable))
        .onSuccess(response -> {
            var array = response.bodyAsJsonArray();
            engine
              .render(new JsonObject().put("countries", array), "templates/countries.html")
              .onFailure(throwable -> log.error("error rendering template", throwable))
              .onSuccess(buffer ->
                context.response().putHeader("content-type", "text/html").end(buffer)
              );
          }
        );
  }

  static Handler<RoutingContext> home(ThymeleafTemplateEngine engine) {
    return context ->
      engine
        .render(new JsonObject().put("version", String.format("Version: %s", VERSION)), "templates/index.html")
        .onFailure(throwable -> log.error("error rendering template", throwable))
        .onSuccess(buffer ->
          context.response().putHeader("content-type", "text/html").end(buffer)
        );
  }
}
