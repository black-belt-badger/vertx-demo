let renderYAML = https://prelude.dhall-lang.org/v23.1.0/JSON/renderYAML.dhall
let render = https://prelude.dhall-lang.org/v23.1.0/JSON/render.dhall
let string = https://prelude.dhall-lang.org/v23.1.0/JSON/string.dhall
let natural = https://prelude.dhall-lang.org/v23.1.0/JSON/natural.dhall
let object = https://prelude.dhall-lang.org/v23.1.0/JSON/object.dhall
let type = https://prelude.dhall-lang.org/v23.1.0/JSON/Type.dhall

let ConfigServer = {
    `config-server`: {
      version : Text,
      host : Text,
      port : Natural
    }
  }

let ConfigServer/ToJSON : ConfigServer -> type
  = \(configServer : ConfigServer)
    -> object ( toMap {
          `config-server` = object ( toMap {
              version = string configServer.config-server.version,
              host    = string configServer.config-server.host,
              port    = natural configServer.config-server.port
             }
            )
          }
        )

let defaultPort = 8887

let dev = {`config-server` = { version = "DEV from config server", host = "localhost", port = defaultPort } }
let devStr : Text = render (ConfigServer/ToJSON dev)

let prod = {`config-server` = { version = "PROD from config server", host = "51.21.163.63", port = defaultPort } }
let prodStr : Text = render (ConfigServer/ToJSON prod)

in {
  dev =  {`config.json` = devStr },
  prod = {`config.json` = prodStr }
}
