let Map =
      https://raw.githubusercontent.com/dhall-lang/dhall-lang/master/Prelude/Map/Type

let type = https://prelude.dhall-lang.org/v23.1.0/JSON/Type.dhall

let string = https://prelude.dhall-lang.org/v23.1.0/JSON/string.dhall

let natural = https://prelude.dhall-lang.org/v23.1.0/JSON/natural.dhall

let object = https://prelude.dhall-lang.org/v23.1.0/JSON/object.dhall

let VertxDemoConfig
    : Type
    = { config-server :
          { host : Text
          , path : Text
          , port : Natural
          , scan-period : Text
          , version : Text
          }
      , `http.port` : Natural
      , `telnet.port` : Natural
      , postgres :
          { host : Text
          , port : Natural
          , database : Text
          , user : Text
          , password : Text
          }
      }

let VertxDemoConfig/ToJSON
    : VertxDemoConfig -> type
    = \(config : VertxDemoConfig) ->
        object
          ( toMap
              { config-server =
                  object
                    ( toMap
                        { host = string config.config-server.host
                        , path = string config.config-server.path
                        , port = natural config.config-server.port
                        , scan-period = string config.config-server.scan-period
                        , version = string config.config-server.version
                        }
                    )
              , `http.port` = natural config.`http.port`
              , postgres =
                  object
                    ( toMap
                        { database = string config.postgres.database
                        , host = string config.postgres.host
                        , password = string config.postgres.password
                        , port = natural config.postgres.port
                        , user = string config.postgres.user
                        }
                    )
              , `telnet.port` = natural config.`telnet.port`
              }
          )

in  { VertxDemoConfig, VertxDemoConfig/ToJSON }
