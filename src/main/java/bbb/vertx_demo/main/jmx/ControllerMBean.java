package bbb.vertx_demo.main.jmx;

public interface ControllerMBean {

  void performOperation(String param);

  String getAttribute();

  void setAttribute(String atrribute);
}
