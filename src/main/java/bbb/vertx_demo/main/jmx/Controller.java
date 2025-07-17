package bbb.vertx_demo.main.jmx;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class Controller implements ControllerMBean {

  private String attribute;

  @Override
  public void performOperation(String parameter) {
    log.info("perform operation {}", parameter);
    setAttribute(parameter);
  }

  @Override
  public String getAttribute() {
    log.info("get attribute {}", attribute);
    return attribute;
  }

  @Override
  public void setAttribute(String attribute) {
    log.info("set attribute from {} to {}", this.attribute, attribute);
    this.attribute = attribute;
  }
}
