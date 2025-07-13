{ services =
  { config-server-nginx =
    { container_name = "config-server-nginx"
    , image = "nginx"
    , ports = [ "8887:80" ]
    , volumes = [ "./configs/dev:/usr/share/nginx/html:rw" ]
    }
  , vertx-demo =
    { command =
        ''
        -conf='{
          "config-server": {
            "host": "host.docker.internal",
            "path": "/conf.json",
            "port": 8887,
            "scan-period": "PT30S",
            "version": "DEV inline"
          },
          "http.port": 8081,
          "telnet.port": 5001
        }${"'"}''
    , container_name = "vertx-demo"
    , environment =
      { JAVA_TOOL_OPTIONS =
          ''
          -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005 -Dlogback.configurationFile=/logs/logback.xml -Dcom.sun.management.jmxremote -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote.port=1099 -Dcom.sun.management.jmxremote.rmi.port=1099 -Djava.rmi.server.hostname=0.0.0.0
          ''
      , VERSION = "\${tag}"
      }
    , image = "marekdudek/vertx-demo:\${tag}"
    , ports = [ "8081:8081", "5005:5005", "5001:5001", "1099:1099" ]
    , volumes = [ "./logs/:/logs/:rw", "./log-data/:/log-data/:rw" ]
    }
  }
}
