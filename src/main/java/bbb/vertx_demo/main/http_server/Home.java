package bbb.vertx_demo.main.http_server;

import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.templ.thymeleaf.ThymeleafTemplateEngine;
import lombok.extern.slf4j.Slf4j;

import static com.google.common.net.MediaType.HTML_UTF_8;
import static java.lang.String.format;
import static java.lang.System.getenv;
import static java.util.Optional.ofNullable;

@Slf4j
public enum Home {

  ;

  private static final String VERSION = ofNullable(getenv("VERSION")).orElse("unknown");
  public static final String HTML = HTML_UTF_8.toString();

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
