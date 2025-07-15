let map =
      https://raw.githubusercontent.com/dhall-lang/dhall-lang/master/Prelude/List/map

let Entry =
      https://raw.githubusercontent.com/dhall-lang/dhall-lang/master/Prelude/Map/Entry

let package = ./imports/compose/v3/package.dhall

let types = ./imports/compose/v3/types.dhall

let defaults = ./imports/compose/v3/defaults.dhall

let render = https://prelude.dhall-lang.org/v23.1.0/JSON/render.dhall

let renderYAML = https://prelude.dhall-lang.org/v23.1.0/JSON/renderYAML.dhall

let string = https://prelude.dhall-lang.org/v23.1.0/JSON/string.dhall

let natural = https://prelude.dhall-lang.org/v23.1.0/JSON/natural.dhall

let object = https://prelude.dhall-lang.org/v23.1.0/JSON/object.dhall

let type = https://prelude.dhall-lang.org/v23.1.0/JSON/Type.dhall

let toEntry =
      \(name : Text) ->
        { mapKey = name
        , mapValue = Some package.Volume::{ driver = Some "local" }
        }

let Output
    : Type
    = Entry Text (Optional package.Volume.Type)

let config-server-nginx =
      package.Service::{
      , container_name = Some "config-server-nginx"
      , image = Some "nginx"
      , ports = Some [ package.StringOrNumber.String "8887:80" ]
      , volumes = Some
        [ package.ServiceVolume.Long
            package.ServiceVolumeLong::{
            , read_only = Some False
            , source = Some "./configs/dev/"
            , target = Some "/usr/share/nginx/html"
            , type = Some "bind"
            }
        ]
      }

let Config =
      { config-server :
          { version : Text
          , host : Text
          , port : Natural
          , path : Text
          , scan-period : Text
          }
      , `http.port` : Natural
      , `telnet.port` : Natural
      }

let Config/ToJSON
    : Config -> type
    = \(config : Config) ->
        object
          ( toMap
              { config-server =
                  object
                    ( toMap
                        { version = string config.config-server.version
                        , host = string config.config-server.host
                        , port = natural config.config-server.port
                        , path = string config.config-server.path
                        , scan-period = string config.config-server.scan-period
                        }
                    )
              , `http.port` = natural config.`http.port`
              , `telnet.port` = natural config.`telnet.port`
              }
          )

let config-server
    : Config
    = { config-server =
        { host = "host.docker.internal"
        , path = "/conf.json"
        , port = 8887
        , scan-period = "PT30S"
        , version = "DEV inline"
        }
      , `http.port` = 8081
      , `telnet.port` = 5001
      }

let config-server-string
    : Text
    = render (Config/ToJSON config-server)

let command
    : Text
    = "-conf='" ++ config-server-string ++ "'"

let vertx-demo =
      package.Service::{
      , command = Some (package.StringOrList.String command)
      , container_name = Some "vertx-demo"
      , environment = Some
          ( package.ListOrDict.Dict
              [ { mapKey = "JAVA_TOOL_OPTIONS"
                , mapValue =
                    ''
                    -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005
                    -Dlogback.configurationFile=/logs/logback.xml
                    -Dcom.sun.management.jmxremote
                    -Dcom.sun.management.jmxremote.authenticate=false
                    -Dcom.sun.management.jmxremote.ssl=false
                    -Dcom.sun.management.jmxremote.port=1099
                    -Dcom.sun.management.jmxremote.rmi.port=1099
                    -Djava.rmi.server.hostname=0.0.0.0
                    ''
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
        [ package.ServiceVolume.Long
            package.ServiceVolumeLong::{
            , read_only = Some False
            , source = Some "./logs/"
            , target = Some "/logs/"
            , type = Some "bind"
            }
        , package.ServiceVolume.Long
            package.ServiceVolumeLong::{
            , read_only = Some False
            , source = Some "./log-data/"
            , target = Some "/log-data/"
            , type = Some "bind"
            }
        ]
      }

let devServices
    : package.Services
    = toMap { config-server-nginx, vertx-demo }

let prodServices
    : package.Services
    = toMap { config-server-nginx, vertx-demo }

let volumes
    : package.Volumes
    = map Text Output toEntry [ "test-volume" ]

in  { dev = package.Config::{ services = Some devServices }
    , prod = package.Config::{ services = Some prodServices }
    }
