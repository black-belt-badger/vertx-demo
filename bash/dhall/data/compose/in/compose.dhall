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

let Environment = < Dev | Prod >

let config-server-nginx =
      \(env : Environment) ->
        package.Service::{
        , container_name = Some "config-server-nginx"
        , image = Some "nginx"
        , ports = Some [ package.StringOrNumber.String "8887:80" ]
        , volumes = Some
          [ package.ServiceVolume.Long
              package.ServiceVolumeLong::{
              , read_only = Some False
              , source = Some "./configs/"
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

let config-server =
      \(env : Environment) ->
        { config-server =
          { host =
              if    merge { Dev = True, Prod = False } env
              then  "host.docker.internal"
              else  "51.21.163.63"
          , path = "/conf.json"
          , port = 8887
          , scan-period =
              if merge { Dev = True, Prod = False } env then "PT5S" else "PT30S"
          , version =
              if    merge { Dev = True, Prod = False } env
              then  "DEV inline"
              else  "PROD inline"
          }
        , `http.port` =
            if merge { Dev = True, Prod = False } env then 8081 else 8080
        , `telnet.port` =
            if merge { Dev = True, Prod = False } env then 5001 else 5000
        }

let config-server-string =
      \(env : Environment) -> render (Config/ToJSON (config-server env))

let command =
      \(env : Environment) -> "-conf='" ++ config-server-string env ++ "'"

let vertx-demo =
      \(env : Environment) ->
        package.Service::{
        , command = Some (package.StringOrList.String (command env))
        , container_name = Some "vertx-demo"
        , environment = Some
            ( package.ListOrDict.Dict
                [ { mapKey = "JAVA_TOOL_OPTIONS"
                  , mapValue =
                      ''
                      -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005
                      -Dlogback.configurationFile=/logs/logback.xml
                      -Djava.net.preferIPv4Stack=true
                      -Dcom.sun.management.jmxremote
                      -Dcom.sun.management.jmxremote.authenticate=false
                      -Dcom.sun.management.jmxremote.ssl=false
                      -Dcom.sun.management.jmxremote.port=1099
                      -Dcom.sun.management.jmxremote.rmi.port=1099
                      -Djava.rmi.server.hostname=${if    merge
                                                           { Dev = True
                                                           , Prod = False
                                                           }
                                                           env
                                                   then  "0.0.0.0"
                                                   else  "ec2-13-60-243-123.eu-north-1.compute.amazonaws.com"}
                      ''
                  }
                , { mapKey = "VERSION", mapValue = "1.0.11" }
                ]
            )
        , image = Some "marekdudek/vertx-demo:1.0.11"
        , ports = Some
          [ package.StringOrNumber.String
              ( if    merge { Dev = True, Prod = False } env
                then  "8081:8081"
                else  "80:8080"
              )
          , package.StringOrNumber.String "5005:5005"
          , package.StringOrNumber.String
              ( if    merge { Dev = True, Prod = False } env
                then  "5001:5001"
                else  "5000:5000"
              )
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

let isDev = \(env : Environment) -> merge { Dev = True, Prod = False } env

let serivces =
      \(env : Environment) ->
        if    merge { Dev = True, Prod = False } env
        then  let config-server-nginx = config-server-nginx Environment.Dev

              let vertx-demo = vertx-demo Environment.Dev

              in  toMap { config-server-nginx, vertx-demo }
        else  let config-server-nginx = config-server-nginx Environment.Prod

              let vertx-demo = vertx-demo Environment.Prod

              in  toMap { config-server-nginx, vertx-demo }

let volumes
    : package.Volumes
    = map Text Output toEntry [ "test-volume" ]

in  { dev = package.Config::{ services = Some (serivces Environment.Dev) }
    , prod = package.Config::{ services = Some (serivces Environment.Prod) }
    }
