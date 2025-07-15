let Map =
      https://raw.githubusercontent.com/dhall-lang/dhall-lang/master/Prelude/Map/Type

let type = https://prelude.dhall-lang.org/v23.1.0/JSON/Type.dhall

let string = https://prelude.dhall-lang.org/v23.1.0/JSON/string.dhall

let natural = https://prelude.dhall-lang.org/v23.1.0/JSON/natural.dhall

let object = https://prelude.dhall-lang.org/v23.1.0/JSON/object.dhall

let VertxDemoConfig
    : Type
    = { config-server :
          { version : Text
          , host : Text
          , port : Natural
          , path : Text
          , scan-period : Text
          }
      , `http.port` : Natural
      , `telnet.port` : Natural
      }

let VertxDemoConfig/ToJSON
    : VertxDemoConfig -> type
    = \(config : VertxDemoConfig) ->
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

in  { VertxDemoConfig, VertxDemoConfig/ToJSON }
