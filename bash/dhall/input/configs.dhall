let renderYAML = https://prelude.dhall-lang.org/v23.1.0/JSON/renderYAML.dhall
let render = https://prelude.dhall-lang.org/v23.1.0/JSON/render.dhall
let Prelude = https://prelude.dhall-lang.org/v23.1.0/package.dhall

let ConfigServer = { `config-server`: Text }

let ConfigServer/ToJSON
  : ConfigServer -> Prelude.JSON.Type
  = \(configServer: ConfigServer)
    -> Prelude.JSON.object
        ( toMap {
          `config-server` = Prelude.JSON.string configServer.config-server
          }
        )

let dev = {`config-server` = "dev value"}
let devJson = ConfigServer/ToJSON dev
let devStr : Text = Prelude.JSON.render devJson

let prod = {`config-server` = "prod value"}
let prodJson = ConfigServer/ToJSON prod
let prodStr : Text = Prelude.JSON.render prodJson

in {
  dev =  {`config.json` = devStr },
  prod = {`config.json` = prodStr }
}
