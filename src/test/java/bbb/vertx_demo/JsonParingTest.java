package bbb.vertx_demo;

import io.vertx.core.json.JsonObject;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
final class JsonParingTest {

  @Test
  void original() {
    var json = new JsonObject("{\"config.version\": \"some value\"}");
    var version = json.getString("config.version");
    assertThat(version).isEqualTo("some value");
  }

  @Test
  void generated() {
    var json = new JsonObject("{\"config\":{\"version\":\"some value\"}}");
    var version = json.getJsonObject("config").getString("version");
    assertThat(version).isEqualTo("some value");
  }
}
