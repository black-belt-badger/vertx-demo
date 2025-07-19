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

let myService = package.Service::{ depends_on = Some [ "one", "two", "three" ] }

let qpid
    : types.DependsOn
    = { condition = Some "service_healthy", restart = Some False }

let postgres
    : types.DependsOn
    = { condition = Some "service_healthy", restart = Some False }

let depends_on_short = types.DependsOnShortOrLong.Short "short version"

let depends_on_long = types.DependsOnShortOrLong.Long qpid

let depends_on_list = [ depends_on_short, depends_on_long ]

let dependencies = [ postgres, qpid ]

let vertxDemo =
      package.Service::{
      , depends_on3 = Some
        [ { mapKey = "postgres"
          , mapValue =
              types.DependsOnShortOrLong.Long
                { condition = Some "service_healthy", restart = Some False }
          }
        , { mapKey = "qpid"
          , mapValue =
              types.DependsOnShortOrLong.Long
                { condition = Some "service_healthy", restart = Some False }
          }
        ]
      }

let config = package.Config::{ services = Some (toMap { vertxDemo }) }

in  { dev = config }
