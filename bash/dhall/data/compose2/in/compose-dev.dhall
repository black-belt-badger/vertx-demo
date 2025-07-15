let map = https://raw.githubusercontent.com/dhall-lang/dhall-lang/master/Prelude/List/map

let Entry = https://raw.githubusercontent.com/dhall-lang/dhall-lang/master/Prelude/Map/Entry

let package  = ./imports/compose/v3/package.dhall
let types    = ./imports/compose/v3/types.dhall
let defaults = ./imports/compose/v3/defaults.dhall

let toEntry = \(name : Text) ->
        { mapKey = name
        , mapValue = Some package.Volume::{ driver = Some "local" }
        }
let Output : Type = Entry Text (Optional package.Volume.Type)

let config-server-nginx = package.Service::{
      , container_name = Some "config-server-nginx"
      , image = Some "nginx"
      , ports = Some [ package.StringOrNumber.String "8887:80" ]
      , volumes = Some
        [ package.ServiceVolume.Long package.ServiceVolumeLong::{
          , read_only = Some False
          , source = Some "./configs/dev/"
          , target = Some "/usr/share/nginx/html"
          , type = Some "bind"
          }
        ]
      }

let nl = "\n"

let command =
    "-conf='{" ++ nl ++
    "  \"config-server\": {" ++ nl ++
    "    \"host\": \"host.docker.internal\"," ++ nl ++
    "    \"path\": \"/conf.json\"," ++ nl ++
    "    \"port\": 8887," ++ nl ++
    "    \"scan-period\": \"PT30S\"," ++ nl ++
    "    \"version\": \"DEV inline\"" ++ nl ++
    "  }," ++ nl ++
    "  \"http.port\": 8081," ++ nl ++
    "  \"telnet.port\": 5001" ++ nl ++
    "}'"

let vertx-demo =  package.Service::{
        command = Some
        ( package.StringOrList.String command )
      , container_name = Some "vertx-demo"
      , environment = Some
      ( package.ListOrDict.Dict
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
        [ package.StringOrNumber.String "8081:8081"
        , package.StringOrNumber.String "5005:5005"
        , package.StringOrNumber.String "5001:5001"
        , package.StringOrNumber.String "1099:1099"
        ]
      , volumes = Some
        [ package.ServiceVolume.Long package.ServiceVolumeLong::{
          , read_only = Some False
          , source = Some "./logs/"
          , target = Some "/logs/"
          , type = Some "bind"
          }
        , package.ServiceVolume.Long package.ServiceVolumeLong::{
          , read_only = Some False
          , source = Some "./log-data/"
          , target = Some "/log-data/"
          , type = Some "bind"
          }
        ]
      }

let services : package.Services
    = toMap {
          config-server-nginx = config-server-nginx
        , vertx-demo = vertx-demo
      }

let volumes : package.Volumes
    = map Text Output toEntry [ "test-volume" ]

in package.Config::{ services = Some services }
