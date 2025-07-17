package bbb.vertx_demo.main;

import bbb.vertx_demo.main.jmx.Controller;
import io.vertx.core.Future;
import lombok.extern.slf4j.Slf4j;

import javax.management.*;
import java.lang.management.ManagementFactory;

import static io.vertx.core.Future.failedFuture;
import static io.vertx.core.Future.succeededFuture;

@Slf4j
public enum MBeanRegistrator {

  ;

  public static Future<ObjectInstance> registerMBean() {
    var name = "bbb.vertx_demo:type=basic,name=vertx-demo";
    log.info("Registering MBean {}", name);
    var mbean = new Controller();
    try {
      var objectName = new ObjectName(name);
      var instance =
        ManagementFactory
          .getPlatformMBeanServer()
          .registerMBean(mbean, objectName);
      return succeededFuture(instance);
    } catch (MalformedObjectNameException |
             InstanceAlreadyExistsException |
             MBeanRegistrationException |
             NotCompliantMBeanException e) {
      return failedFuture(e);
    }
  }
}
