package bbb.vertx_demo.main.http_server;

import com.google.common.base.Stopwatch;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.templ.thymeleaf.ThymeleafTemplateEngine;
import io.vertx.redis.client.RedisAPI;
import io.vertx.redis.client.RedisConnection;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;

import static bbb.vertx_demo.main.http_server.HttpServerStarter.*;
import static com.google.common.net.MediaType.HTML_UTF_8;
import static io.vertx.core.http.HttpHeaders.CACHE_CONTROL;
import static io.vertx.core.http.HttpHeaders.CONTENT_TYPE;
import static io.vertx.redis.client.Command.SETEX;
import static io.vertx.redis.client.Request.cmd;
import static java.lang.String.format;
import static java.lang.System.getenv;
import static java.util.Comparator.comparing;
import static java.util.Objects.nonNull;
import static java.util.Optional.ofNullable;

@Slf4j
public enum Handlers {

  ;

  private static final String VERSION = ofNullable(getenv("VERSION")).orElse("unknown");
  private static final String HTML = HTML_UTF_8.toString();

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

  private static final String COUNTRIES_REDIS_KEY = "/api/v1/country";
  private static final String COUNTRIES_TEMPLATE_KEY = "countries";

  static Handler<RoutingContext> countries
    (
      WebClient webClient,
      ThymeleafTemplateEngine engine,
      RedisAPI redisAPI,
      RedisConnection redisConnection,
      JsonObject countries
    ) {
    return context -> {
      var maxAgeString = countries.getString("max-age", "PT1H");
      var maxAge = Duration.parse(maxAgeString).toSeconds();
      log.info("Cache expiry for countries is {} seconds", maxAge);
      var cacheControl = format("public, max-age=%d, immutable", maxAge);
      var watch = Stopwatch.createStarted();
      redisAPI
        .get(COUNTRIES_REDIS_KEY)
        .onFailure(throwable -> log.error("error getting countries from Redis", throwable))
        .onSuccess(redisResponse -> {
            if (nonNull(redisResponse)) {
              var buffer = redisResponse.toBuffer();
              context.response()
                .putHeader(CONTENT_TYPE, HTML)
                .putHeader(CACHE_CONTROL, cacheControl)
                .end(buffer);
              log.info("Countries request handled in {}", watch.elapsed());
            } else {
              webClient
                .get(FINNHUB_PORT, FINNHUB_HOST, COUNTRIES_REDIS_KEY)
                .putHeader(FINNHUB_HEADER, FINNHUB_API_KEY)
                .send()
                .onFailure(throwable -> log.error("error sending request", throwable))
                .onSuccess(response -> {
                    var array = response.bodyAsJsonArray();
                    var sorted =
                      array.stream()
                        .map(o -> (JsonObject) o)
                        .sorted(
                          comparing(obj -> obj.getString("country").toLowerCase())
                        )
                        .toList();
                    var map =
                      new JsonArray(sorted).stream().map(o ->
                          ((JsonObject) o).getMap()
                        )
                        .toList();
                    engine
                      .render(new JsonObject().put(COUNTRIES_TEMPLATE_KEY, map), "templates/countries.html")
                      .onFailure(throwable -> log.error("error rendering template", throwable))
                      .onSuccess(buffer -> {
                          context.response().putHeader(CONTENT_TYPE, HTML).end(buffer);
                          log.info("Countries request handled in {}", watch.elapsed());
                          var request = cmd(SETEX)
                            .arg(COUNTRIES_REDIS_KEY)
                            .arg(maxAge)
                            .arg(buffer);
                          redisConnection.send(request);
                        }
                      );
                  }
                );
            }
          }
        );
    };
  }

  static Handler<RoutingContext> home(ThymeleafTemplateEngine engine) {
    return context ->
      engine
        .render(new JsonObject().put("version", format("Version: %s", VERSION)), "templates/index.html")
        .onFailure(throwable -> log.error("error rendering template", throwable))
        .onSuccess(buffer ->
          context.response().putHeader("content-type", "text/html").end(buffer)
        );
  }
}
