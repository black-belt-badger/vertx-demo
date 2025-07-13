let renderYAML = https://prelude.dhall-lang.org/v23.1.0/JSON/renderYAML.dhall
let render = https://prelude.dhall-lang.org/v23.1.0/JSON/render.dhall
let Prelude = https://prelude.dhall-lang.org/v23.1.0/package.dhall

let ConfigServer = { `config-server`: { version : Text } }

let ConfigServer/ToJSON
  : ConfigServer -> Prelude.JSON.Type
  = \(configServer: ConfigServer)
    -> Prelude.JSON.object
        ( toMap {
          `config-server` = Prelude.JSON.object
            ( toMap {
              version = Prelude.JSON.string configServer.config-server.version
             }
            )
          }
        )

let dev = {`config-server` = { version = "DEV from config server" } }
let devJson = ConfigServer/ToJSON dev
let devStr : Text = Prelude.JSON.render devJson

let prod = {`config-server` = { version = "PROD from config server"} }
let prodJson = ConfigServer/ToJSON prod
let prodStr : Text = Prelude.JSON.render prodJson

in {
  dev =  {`config.json` = devStr },
  prod = {`config.json` = prodStr }
}
