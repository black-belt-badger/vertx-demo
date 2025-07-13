package bbb.vertx_demo;

import io.vertx.core.json.JsonObject;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

@Slf4j
public class JsonParingTest {
  @Test
  void original() {
    var original =
      new JsonObject("{" +
        "  \"config-server.version\": \"DEV from config server\"" +
        "}");
    log.info("Original: {}", original.encodePrettily());
  }

  @Test
  void generated() {
    var generated =
      new JsonObject("{" +
        "  \"config-server\": {" +
        "    \"version\": \"DEV from config server\"" +
        "  }" +
        "}");
    log.info("Generated: {}", generated.encodePrettily());
  }
}
