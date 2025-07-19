package bbb.vertx_demo.main;

import bbb.vertx_demo.main.jmx.Controller;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.ext.web.healthchecks.HealthCheckHandler;
import lombok.extern.slf4j.Slf4j;

import javax.management.*;
import java.lang.management.ManagementFactory;

import static io.vertx.core.Future.failedFuture;
import static io.vertx.core.Future.succeededFuture;
import static io.vertx.ext.healthchecks.Status.KO;

@Slf4j
public enum MBeanRegistrator {

  ;

  private static final String MBEAN_REGISTRATION = "mbean-registration";

  public static Future<ObjectInstance> registerMBean(HealthCheckHandler checks) {
    var name = "bbb.vertx_demo:type=basic,name=vertx-demo";
    log.info("Registering MBean {}", name);
    var mbean = new Controller();
    try {
      var objectName = new ObjectName(name);
      var instance =
        ManagementFactory
          .getPlatformMBeanServer()
          .registerMBean(mbean, objectName);
      checks.register(MBEAN_REGISTRATION, Promise::succeed);
      return succeededFuture(instance);
    } catch (MalformedObjectNameException |
             InstanceAlreadyExistsException |
             MBeanRegistrationException |
             NotCompliantMBeanException e) {
      checks.register(MBEAN_REGISTRATION, promise ->
        promise.complete(KO(), e)
      );
      return failedFuture(e);
    }
  }
}
