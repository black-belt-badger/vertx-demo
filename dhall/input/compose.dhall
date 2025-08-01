let map =
      https://raw.githubusercontent.com/dhall-lang/dhall-lang/master/Prelude/List/map

let Entry =
      https://raw.githubusercontent.com/dhall-lang/dhall-lang/master/Prelude/Map/Entry

let package = ../imports/compose/v3/package.dhall

let types = ../imports/compose/v3/types.dhall

let defaults = ../imports/compose/v3/defaults.dhall

let render = https://prelude.dhall-lang.org/v23.1.0/JSON/render.dhall

let renderYAML = https://prelude.dhall-lang.org/v23.1.0/JSON/renderYAML.dhall

let string = https://prelude.dhall-lang.org/v23.1.0/JSON/string.dhall

let natural = https://prelude.dhall-lang.org/v23.1.0/JSON/natural.dhall

let object = https://prelude.dhall-lang.org/v23.1.0/JSON/object.dhall

let type = https://prelude.dhall-lang.org/v23.1.0/JSON/Type.dhall

let vdc = ../imports/vertx-demo-config/vdc.dhall

let version = "1.0.32.1"

let Environment = < Dev | Prod >

let Output
    : Type
    = Entry Text (Optional package.Volume.Type)

let toEntry =
      \(name : Text) ->
        { mapKey = name
        , mapValue = Some package.Volume::{ driver = None Text }
        }

let dev_db_name = "vertx_demo_dev_database"

let dev_db_user = "vertx_demo_dev_user"

let dev_db_password = "vertx_demo_dev_password"

let dev_qpid_admin_username = "dev_admin"

let dev_qpid_admin_password = "dev_secret"

let prod_qpid_admin_username = "prod_admin"

let prod_qpid_admin_password = "prod_secret"

let dev_default_cache_max_age = "PT1S"

let prod_default_cache_max_age = "PT1M"

let amqp_queue_delay = 600000

let service_healthy =
      package.DependsOn.Long
        package.DependsOnLong::{ condition = Some "service_healthy" }

let service_completed_successfully =
      package.DependsOn.Long
        package.DependsOnLong::{
        , condition = Some "service_completed_successfully"
        }

let config-server-nginx =
      \(env : Environment) ->
        package.Service::{
        , container_name = Some "config-server-nginx"
        , image = Some "nginx"
        , healthcheck = Some package.Healthcheck::{
          , interval = Some "10s"
          , retries = Some 10
          , test = Some
              (package.StringOrList.String "service nginx status || exit 1")
          , timeout = Some "1s"
          }
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

let pgadmin =
      \(env : Environment) ->
        package.Service::{
        , container_name = Some "pgadmin"
        , depends_on = Some
          [ { mapKey = "postgres", mapValue = service_healthy } ]
        , environment = Some
            ( package.ListOrDict.Dict
                [ { mapKey = "PGADMIN_DEFAULT_EMAIL"
                  , mapValue = "user@domain.com"
                  }
                , { mapKey = "PGADMIN_DEFAULT_PASSWORD", mapValue = "secret" }
                ]
            )
        , image = Some "dpage/pgadmin4"
        , ports = Some [ package.StringOrNumber.String "5050:80" ]
        , volumes = Some
          [ package.ServiceVolume.Long
              package.ServiceVolumeLong::{
              , read_only = Some False
              , source = Some "./pgadmin/servers.json"
              , target = Some "/pgadmin4/servers.json"
              , type = Some "bind"
              }
          ]
        , restart = Some "unless-stopped"
        }

let postgres =
      \(env : Environment) ->
        package.Service::{
        , container_name = Some "postgres"
        , environment = Some
            ( package.ListOrDict.Dict
                [ { mapKey = "POSTGRES_DB", mapValue = dev_db_name }
                , { mapKey = "POSTGRES_PASSWORD", mapValue = dev_db_password }
                , { mapKey = "POSTGRES_USER", mapValue = dev_db_user }
                ]
            )
        , healthcheck = Some package.Healthcheck::{
          , interval = Some "3s"
          , retries = Some 10
          , test = Some
              ( package.StringOrList.String
                  "pg_isready -U ${dev_db_user} -d ${dev_db_name}"
              )
          , timeout = Some "1s"
          }
        , image = Some "postgres"
        , ports = Some [ package.StringOrNumber.String "5432:5432" ]
        , restart = Some "unless-stopped"
        }

let psql =
      package.Service::{
      , container_name = Some "psql"
      , command = Some
          ( package.StringOrList.String
              ''
              --dbname=${dev_db_name} --username ${dev_db_user} --host postgres --port 5432  --echo-all --single-transaction
              --file sql/init-db.sql
              ''
          )
      , depends_on = Some
        [ { mapKey = "postgres", mapValue = service_healthy } ]
      , environment = Some
          ( package.ListOrDict.Dict
              [ { mapKey = "PGPASSWORD", mapValue = dev_db_password } ]
          )
      , image = Some "alpine/psql"
      , volumes = Some
        [ package.ServiceVolume.Long
            package.ServiceVolumeLong::{
            , read_only = Some False
            , source = Some "./sql"
            , target = Some "/sql"
            , type = Some "bind"
            }
        ]
      }

let logs-volume =
      package.ServiceVolume.Long
        package.ServiceVolumeLong::{
        , read_only = Some False
        , source = Some "./logs/"
        , target = Some "/logs/"
        , type = Some "bind"
        }

let logs-data-volume =
      package.ServiceVolume.Long
        package.ServiceVolumeLong::{
        , read_only = Some False
        , source = Some "./log-data/"
        , target = Some "/log-data/"
        , type = Some "bind"
        }

let lets-encrypt-volume =
      package.ServiceVolume.Long
        package.ServiceVolumeLong::{
        , read_only = Some True
        , source = Some "/etc/letsencrypt"
        , target = Some "/etc/letsencrypt"
        , type = Some "bind"
        }

let config-server =
      \(env : Environment) ->
        if    merge { Dev = True, Prod = False } env
        then  { amqp =
                { client = { delay = amqp_queue_delay, queue = "client-queue" }
                , host = "qpid"
                , password = dev_qpid_admin_password
                , port = 5672
                , reconnect-attempts = 2147483647
                , reconnect-interval = 100
                , server = { delay = amqp_queue_delay, queue = "server-queue" }
                , username = dev_qpid_admin_username
                }
              , config-server =
                { host = "host.docker.internal"
                , path = "/conf.json"
                , port = 8887
                , scan-period = "PT5S"
                , version = "DEV inline"
                }
              , http =
                { secure-port = 8444
                , insecure-port = 8081
                , key-path = "security/smooth-all/server.key.pem"
                , cert-path = "security/smooth-all/server.cert.pem"
                , cache =
                  { home.max-age = dev_default_cache_max_age
                  , about.max-age = dev_default_cache_max_age
                  , ipos.max-age = dev_default_cache_max_age
                  , general-news.max-age = dev_default_cache_max_age
                  , forex-news.max-age = dev_default_cache_max_age
                  , crypto-news.max-age = dev_default_cache_max_age
                  , merger-news.max-age = dev_default_cache_max_age
                  }
                }
              , postgres =
                { database = dev_db_name
                , default-schema = "finnhub"
                , environment = "dev"
                , host = "host.docker.internal"
                , password = dev_db_password
                , port = 5432
                , ssl-mode = "disable"
                , trust-all = False
                , user = dev_db_user
                }
              , `redis.host` = "redis"
              , `telnet.port` = 5001
              , updater =
                { ipo-updater-delay = "PT1H"
                , news-general-updater-delay = "PT1M"
                , news-forex-updater-delay = "PT1M"
                , news-crypto-updater-delay = "PT1M"
                , news-merger-updater-delay = "PT1M"
                }
              }
        else  { amqp =
                { client = { delay = amqp_queue_delay, queue = "client-queue" }
                , host = "qpid"
                , password = prod_qpid_admin_password
                , port = 5672
                , reconnect-attempts = 2147483647
                , reconnect-interval = 100
                , server = { delay = amqp_queue_delay, queue = "server-queue" }
                , username = prod_qpid_admin_username
                }
              , config-server =
                { host = "51.21.163.63"
                , path = "/conf.json"
                , port = 8887
                , scan-period = "PT30S"
                , version = "PROD inline"
                }
              , http =
                { secure-port = 8443
                , insecure-port = 8080
                , key-path = "/etc/letsencrypt/live/9rove.com/privkey.pem"
                , cert-path = "/etc/letsencrypt/live/9rove.com/fullchain.pem"
                , cache =
                  { home.max-age = prod_default_cache_max_age
                  , about.max-age = prod_default_cache_max_age
                  , ipos.max-age = prod_default_cache_max_age
                  , general-news.max-age = prod_default_cache_max_age
                  , forex-news.max-age = prod_default_cache_max_age
                  , crypto-news.max-age = prod_default_cache_max_age
                  , merger-news.max-age = prod_default_cache_max_age
                  }
                }
              , postgres =
                { database = "postgres"
                , default-schema = "finnhub"
                , environment = "prod"
                , host = "grove-db.chimcku4qngw.eu-north-1.rds.amazonaws.com"
                , password = "vertx_demo_password"
                , port = 5432
                , ssl-mode = "allow"
                , trust-all = True
                , user = "vertx_demo_admin"
                }
              , `redis.host` = "redis"
              , `telnet.port` = 5000
              , updater =
                { ipo-updater-delay = "PT1H"
                , news-general-updater-delay = "PT1M"
                , news-forex-updater-delay = "PT1M"
                , news-crypto-updater-delay = "PT1M"
                , news-merger-updater-delay = "PT1M"
                }
              }

let network-test =
      \(env : Environment) ->
        package.Service::{
        , command = Some
            ( if    merge { Dev = True, Prod = False } env
              then  package.StringOrList.String
                      "bbb.vertx_demo.NetworkTestVerticle"
              else  package.StringOrList.String
                      ''
                      bbb.vertx_demo.NetworkTestVerticle
                      -conf='{"key-path": "/etc/letsencrypt/live/9rove.com/privkey.pem", "cert-path": "/etc/letsencrypt/live/9rove.com/fullchain.pem" }'
                      ''
            )
        , container_name = Some "network-test"
        , environment = Some
            ( package.ListOrDict.Dict
                [ { mapKey = "JAVA_TOOL_OPTIONS"
                  , mapValue =
                      ''
                      -Dlogback.configurationFile=/logs/logback.xml
                      -Djava.net.preferIPv4Stack=true
                      ''
                  }
                ]
            )
        , image = Some ("marekdudek/vertx-demo:" ++ version)
        , ports = Some
          [ package.StringOrNumber.String "18080:18080"
          , package.StringOrNumber.String "18443:18443"
          ]
        , volumes = Some
            ( if    merge { Dev = True, Prod = False } env
              then  [ logs-volume, logs-data-volume ]
              else  [ logs-volume, logs-data-volume, lets-encrypt-volume ]
            )
        }

let qpid =
      \(env : Environment) ->
        let admin_username =
              if    merge { Dev = True, Prod = False } env
              then  dev_qpid_admin_username
              else  prod_qpid_admin_username

        let admin_password =
              if    merge { Dev = True, Prod = False } env
              then  dev_qpid_admin_password
              else  prod_qpid_admin_password

        in  package.Service::{
            , container_name = Some "qpid"
            , environment = Some
                ( package.ListOrDict.Dict
                    [ { mapKey = "JAVA_GC", mapValue = "-XX:+UseG1GC" }
                    , { mapKey = "JAVA_MEM"
                      , mapValue = "-Xmx256m -XX:MaxDirectMemorySize=128m"
                      }
                    , { mapKey = "JAVA_OPTS", mapValue = "" }
                    , { mapKey = "QPID_ADMIN_PASSWORD"
                      , mapValue = admin_password
                      }
                    , { mapKey = "QPID_ADMIN_USER", mapValue = admin_username }
                    ]
                )
            , healthcheck = Some package.Healthcheck::{
              , interval = Some "5s"
              , retries = Some 3
              , test = Some
                  ( package.StringOrList.String
                      "curl -u ${admin_username}:${admin_password} --basic -o /dev/null -f -w %{http_code} http://${admin_username}:${admin_password}@localhost:8080/api/latest/broker"
                  )
              , timeout = Some "3s"
              }
            , image = Some "apache/qpid-broker-j"
            , ports = Some
              [ package.StringOrNumber.String "5672:5672"
              , package.StringOrNumber.String "15672:8080"
              ]
            , volumes = Some
              [ package.ServiceVolume.Long
                  package.ServiceVolumeLong::{
                  , read_only = Some False
                  , source = Some "./qpid/default.json"
                  , target = Some "/qpid-broker-j/work-init/default.json"
                  , type = Some "bind"
                  }
              ]
            }

let redis_data
    : Text
    = "redis_data"

let redis =
      \(env : Environment) ->
        package.Service::{
        , command = Some
            (package.StringOrList.String "redis-server --appendonly yes")
        , container_name = Some "redis"
        , image = Some "redis:latest"
        , ports = Some [ package.StringOrNumber.String "6379:6379" ]
        , restart = Some "unless-stopped"
        , volumes = Some
          [ package.ServiceVolume.Short (redis_data ++ ":/data") ]
        }

let config-server-string =
      \(env : Environment) ->
        render (vdc.VertxDemoConfig/ToJSON (config-server env))

let command =
      \(env : Environment) -> "-conf='" ++ config-server-string env ++ "'"

let vertx-demo =
      \(env : Environment) ->
        package.Service::{
        , command = Some (package.StringOrList.String (command env))
        , container_name = Some "vertx-demo"
        , depends_on =
            if    merge { Dev = True, Prod = False } env
            then  Some
                    [ { mapKey = "config-server-nginx"
                      , mapValue = service_healthy
                      }
                    , { mapKey = "postgres", mapValue = service_healthy }
                    , { mapKey = "psql"
                      , mapValue = service_completed_successfully
                      }
                    , { mapKey = "qpid", mapValue = service_healthy }
                    ]
            else  Some
                    [ { mapKey = "config-server-nginx"
                      , mapValue = service_healthy
                      }
                    , { mapKey = "qpid", mapValue = service_healthy }
                    ]
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
                                                   else  "ec2-51-21-163-63.eu-north-1.compute.amazonaws.com"}
                      ''
                  }
                , { mapKey = "VERSION", mapValue = version }
                ]
            )
        , healthcheck = Some package.Healthcheck::{
          , interval = Some "10s"
          , retries = Some 10
          , test = Some
              ( package.StringOrList.String
                  "curl -f http://localhost:8081/health"
              )
          , timeout = Some "1s"
          }
        , image = Some ("marekdudek/vertx-demo:" ++ version)
        , ports = Some
          [ package.StringOrNumber.String
              ( if    merge { Dev = True, Prod = False } env
                then  "8081:8081"
                else  "80:8080"
              )
          , package.StringOrNumber.String
              ( if    merge { Dev = True, Prod = False } env
                then  "8444:8444"
                else  "443:8443"
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
            ( if    merge { Dev = True, Prod = False } env
              then  [ logs-volume, logs-data-volume ]
              else  [ logs-volume, logs-data-volume, lets-encrypt-volume ]
            )
        }

let serivces =
      \(env : Environment) ->
        if    merge { Dev = True, Prod = False } env
        then  let config-server-nginx = config-server-nginx env

              let vertx-demo = vertx-demo env

              let pgadmin = pgadmin env

              let postgres = postgres env

              let redis = redis env

              let qpid = qpid env

              let network-test = network-test env

              in  toMap
                    { config-server-nginx
                    , vertx-demo
                    , pgadmin
                    , postgres
                    , psql
                    , qpid
                    , redis
                    , network-test
                    }
        else  let config-server-nginx = config-server-nginx env

              let vertx-demo = vertx-demo env

              let redis = redis env

              let qpid = qpid env

              let network-test = network-test env

              in  toMap
                    { config-server-nginx
                    , vertx-demo
                    , qpid
                    , redis
                    , network-test
                    }

let volumes
    : package.Volumes
    = map Text Output toEntry [ redis_data ]

in  { dev = package.Config::{
      , services = Some (serivces Environment.Dev)
      , volumes = Some volumes
      }
    , prod = package.Config::{
      , services = Some (serivces Environment.Prod)
      , volumes = Some volumes
      }
    }
