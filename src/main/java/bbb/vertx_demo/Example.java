package bbb.vertx_demo;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class Example implements ExampleMBean {

  private String attribute;

  @Override
  public void performOperation(String parameter) {
    log.info("perform operation {}", parameter);
  }

  @Override
  public String getAttribute() {
    log.info("get attribute {}", attribute);
    return attribute;
  }

  @Override
  public void setAttribute(String attribute) {
    log.info("set attribute {}", attribute);
    this.attribute = attribute;
  }
}
