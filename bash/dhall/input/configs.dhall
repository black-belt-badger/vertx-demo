let renderYAML = https://prelude.dhall-lang.org/v23.1.0/JSON/renderYAML.dhall
let render = https://prelude.dhall-lang.org/v23.1.0/JSON/render.dhall
let Prelude = https://prelude.dhall-lang.org/v23.1.0/package.dhall

let ConfigServer = {
    `config-server`: {
      version : Text,
      host : Text
    }
  }

let ConfigServer/ToJSON
  : ConfigServer -> Prelude.JSON.Type
  = \(configServer: ConfigServer)
    -> Prelude.JSON.object
        ( toMap {
          `config-server` = Prelude.JSON.object
            ( toMap {
              version = Prelude.JSON.string configServer.config-server.version,
              host    = Prelude.JSON.string configServer.config-server.host
             }
            )
          }
        )

let dev = {`config-server` = { version = "DEV from config server", host = "localhost" } }
let devStr : Text = Prelude.JSON.render (ConfigServer/ToJSON dev)

let prod = {`config-server` = { version = "PROD from config server", host = "51.21.163.63" } }
let prodStr : Text = Prelude.JSON.render (ConfigServer/ToJSON prod)

in {
  dev =  {`config.json` = devStr },
  prod = {`config.json` = prodStr }
}
