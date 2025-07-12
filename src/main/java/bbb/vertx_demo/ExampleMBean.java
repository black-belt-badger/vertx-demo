package bbb.vertx_demo;

public interface ExampleMBean {

  void performOperation(String param);

  String getAttribute();

  void setAttribute(String atrribute);
}
