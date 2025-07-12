package bbb.vertx_demo;

public interface ControllerMBean {

  void performOperation(String param);

  String getAttribute();

  void setAttribute(String atrribute);
}
