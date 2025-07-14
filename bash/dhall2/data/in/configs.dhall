let render = https://prelude.dhall-lang.org/v23.1.0/JSON/render.dhall
let string = https://prelude.dhall-lang.org/v23.1.0/JSON/string.dhall
let natural = https://prelude.dhall-lang.org/v23.1.0/JSON/natural.dhall
let object = https://prelude.dhall-lang.org/v23.1.0/JSON/object.dhall
let type = https://prelude.dhall-lang.org/v23.1.0/JSON/Type.dhall

let Config = {
    config-server : {
      version     : Text,
      host        : Text,
      port        : Natural,
      path        : Text,
      scan-period : Text
    }
  }

let Config/ToJSON : Config -> type
  = \(config : Config)
    -> object ( toMap {
          config-server = object ( toMap {
              version     = string  config.config-server.version,
              host        = string  config.config-server.host,
              port        = natural config.config-server.port,
              path        = string  config.config-server.path,
              scan-period = string  config.config-server.scan-period
             }
            )
          }
        )

let defaultHost = "localhost"
let defaultPort = 8887
let defaultPath = "/conf.json"
let defaultScanPeriod = "PT30S"

let dev  = {
  config-server = {
      version = "DEV from config server",
      host = "host.docker.internal",
      port = defaultPort,
      path = defaultPath,
      scan-period = "PT5S"
    }
  }
let prod = {
  config-server = {
      version = "PROD from config server",
      host = "51.21.163.63",
      port = defaultPort,
      path = defaultPath,
      scan-period = defaultScanPeriod
    }
  }

in {
  dev =  { `conf.json` = render (Config/ToJSON dev)  },
  prod = { `conf.json` = render (Config/ToJSON prod) }
}
