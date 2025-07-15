let map = https://raw.githubusercontent.com/dhall-lang/dhall-lang/master/Prelude/List/map

let Entry = https://raw.githubusercontent.com/dhall-lang/dhall-lang/master/Prelude/Map/Entry

let Compose  = ./imports/compose/v3/package.dhall
let types    = ./imports/compose/v3/types.dhall
let defaults = ./imports/compose/v3/defaults.dhall

let toEntry = \(name : Text) ->
        { mapKey = name
        , mapValue = Some Compose.Volume::{ driver = Some "local" }
        }
let Output : Type = Entry Text (Optional Compose.Volume.Type)

let config-server-nginx = Compose.Service::{
      , container_name = Some "config-server-nginx"
      , image = Some "nginx"
      , ports = Some [ Compose.StringOrNumber.String "8887:80" ]
      , volumes = Some
        [ Compose.ServiceVolume.Short "./configs/dev:/usr/share/nginx/html:rw"
        ]
      }

let nl = "\n"

let vertx-demo =  Compose.Service::{
      , container_name = Some "vertx-demo"
      , environment = Some
      ( Compose.ListOrDict.Dict
        [ { mapKey = "JAVA_TOOL_OPTIONS", mapValue =
            "-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005" ++ nl ++
            "-Dlogback.configurationFile=/logs/logback.xml" ++ nl ++
            "-Dcom.sun.management.jmxremote" ++ nl ++
            "-Dcom.sun.management.jmxremote.authenticate=false" ++ nl ++
            "-Dcom.sun.management.jmxremote.ssl=false" ++ nl ++
            "-Dcom.sun.management.jmxremote.port=1099" ++ nl ++
            "-Dcom.sun.management.jmxremote.rmi.port=1099" ++ nl ++
            "-Djava.rmi.server.hostname=0.0.0.0"
           }
        , { mapKey = "VERSION", mapValue = "1.0.11" }
        ]
      )
      , image = Some "marekdudek/vertx-demo:1.0.11"
      , ports = Some
        [ Compose.StringOrNumber.String "8081:8081"
        , Compose.StringOrNumber.String "5005:5005"
        , Compose.StringOrNumber.String "5001:5001"
        , Compose.StringOrNumber.String "1099:1099"
        ]
      , volumes = Some
        [ Compose.ServiceVolume.Short "./logs/:/logs/:rw"
        , Compose.ServiceVolume.Short "./log-data/:/log-data/:rw"
        ]
      }

let services : Compose.Services
    = toMap {
          config-server-nginx = config-server-nginx
        , vertx-demo = vertx-demo
      }

let volumes : Compose.Volumes
    = map Text Output toEntry [ "test-volume" ]

in Compose.Config::{ services = Some services }
