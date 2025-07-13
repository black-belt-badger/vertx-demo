let renderYAML = https://prelude.dhall-lang.org/v23.1.0/JSON/renderYAML.dhall

let dev = "{}"
let prod = "{}"

in  { `config-dev` = dev
    , `config-prod` = prod
    }
