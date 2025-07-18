let Map =
      https://raw.githubusercontent.com/dhall-lang/dhall-lang/master/Prelude/Map/Type

let type = https://prelude.dhall-lang.org/v23.1.0/JSON/Type.dhall

let string = https://prelude.dhall-lang.org/v23.1.0/JSON/string.dhall

let natural = https://prelude.dhall-lang.org/v23.1.0/JSON/natural.dhall

let object = https://prelude.dhall-lang.org/v23.1.0/JSON/object.dhall

let VertxDemoConfig
    : Type
    = { amqp :
          { client : { delay : Natural, queue : Text }
          , host : Text
          , password : Text
          , port : Natural
          , reconnect-attempts : Natural
          , reconnect-interval : Natural
          , server : { delay : Natural, queue : Text }
          , username : Text
          }
      , config-server :
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
              { amqp =
                  object
                    ( toMap
                        { client =
                            object
                              ( toMap
                                  { delay = natural config.amqp.client.delay
                                  , queue = string config.amqp.client.queue
                                  }
                              )
                        , host = string config.amqp.host
                        , port = natural config.amqp.port
                        , password = string config.amqp.password
                        , reconnect-attempts =
                            natural config.amqp.reconnect-attempts
                        , reconnect-interval =
                            natural config.amqp.reconnect-interval
                        , server =
                            object
                              ( toMap
                                  { delay = natural config.amqp.server.delay
                                  , queue = string config.amqp.server.queue
                                  }
                              )
                        , username = string config.amqp.username
                        }
                    )
              , config-server =
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
