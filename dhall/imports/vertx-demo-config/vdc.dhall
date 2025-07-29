let Map =
      https://raw.githubusercontent.com/dhall-lang/dhall-lang/master/Prelude/Map/Type

let type = https://prelude.dhall-lang.org/v23.1.0/JSON/Type.dhall

let string = https://prelude.dhall-lang.org/v23.1.0/JSON/string.dhall

let natural = https://prelude.dhall-lang.org/v23.1.0/JSON/natural.dhall

let bool = https://prelude.dhall-lang.org/v23.1.0/JSON/bool.dhall

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
      , http :
          { secure-port : Natural
          , insecure-port : Natural
          , key-path : Text
          , cert-path : Text
          , cache :
              { home : { max-age : Text }
              , about : { max-age : Text }
              , ipos : { max-age : Text }
              , general-news : { max-age : Text }
              }
          }
      , postgres :
          { database : Text
          , host : Text
          , password : Text
          , port : Natural
          , ssl-mode : Text
          , trust-all : Bool
          , user : Text
          }
      , `redis.host` : Text
      , `telnet.port` : Natural
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
              , http =
                  object
                    ( toMap
                        { secure-port = natural config.http.secure-port
                        , insecure-port = natural config.http.insecure-port
                        , key-path = string config.http.key-path
                        , cert-path = string config.http.cert-path
                        , cache =
                            object
                              ( toMap
                                  { home =
                                      object
                                        ( toMap
                                            { max-age =
                                                string
                                                  config.http.cache.home.max-age
                                            }
                                        )
                                  , about =
                                      object
                                        ( toMap
                                            { max-age =
                                                string
                                                  config.http.cache.about.max-age
                                            }
                                        )
                                  , ipos =
                                      object
                                        ( toMap
                                            { max-age =
                                                string
                                                  config.http.cache.ipos.max-age
                                            }
                                        )
                                  , general-news =
                                      object
                                        ( toMap
                                            { max-age =
                                                string
                                                  config.http.cache.general-news.max-age
                                            }
                                        )
                                  }
                              )
                        }
                    )
              , postgres =
                  object
                    ( toMap
                        { database = string config.postgres.database
                        , host = string config.postgres.host
                        , password = string config.postgres.password
                        , port = natural config.postgres.port
                        , ssl-mode = string config.postgres.ssl-mode
                        , trust-all = bool config.postgres.trust-all
                        , user = string config.postgres.user
                        }
                    )
              , `redis.host` = string config.`redis.host`
              , `telnet.port` = natural config.`telnet.port`
              }
          )

in  { VertxDemoConfig, VertxDemoConfig/ToJSON }
