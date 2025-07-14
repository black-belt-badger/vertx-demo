let render = https://prelude.dhall-lang.org/v23.1.0/JSON/render.dhall
let renderYAML = https://prelude.dhall-lang.org/v23.1.0/JSON/renderYAML.dhall
let string = https://prelude.dhall-lang.org/v23.1.0/JSON/string.dhall
let natural = https://prelude.dhall-lang.org/v23.1.0/JSON/natural.dhall
let double = https://prelude.dhall-lang.org/v23.1.0/JSON/double.dhall
let bool = https://prelude.dhall-lang.org/v23.1.0/JSON/bool.dhall
let object = https://prelude.dhall-lang.org/v23.1.0/JSON/object.dhall
let array = https://prelude.dhall-lang.org/v23.1.0/JSON/array.dhall
let type = https://prelude.dhall-lang.org/v23.1.0/JSON/Type.dhall

let Compose = {
  services : Text
}

let dev = {
  services = "my two services"
}

let Compose/ToJSON : Compose -> type
  = \(compose : Compose)
  -> object ( toMap {
        services = string compose.services
      }
    )

in {
  -- dev  = { `compose.yaml` = renderYAML (Compose/ToJSON dev) },
  dev  = { `compose.yaml` =
    renderYAML (
      array [
        string "services"
      ]
      )
    },
  prod = { `compose.yaml` =
   renderYAML (
         string ("title")
      )
   },
  test = { `compose.yaml` =
    renderYAML
        ( object
          [ { mapKey = "foo", mapValue = double 1.0 }
          , { mapKey = "bar", mapValue = bool True  }
          ]
        )
    },
  test2 = { `compose.yaml` =
        render
            ( object
              [ { mapKey = "foo", mapValue = double 1.0 }
              , { mapKey = "bar", mapValue = bool True  }
              ]
            )
        },
  test3 = { `compose.yaml` =
    renderYAML
        (object ([] : List { mapKey : Text, mapValue : type }))
    },
  test4 = { `compose.yaml` =
    renderYAML
            (  object [{
                mapKey = "services", mapValue =
                  object
                  [ { mapKey = "vertx-demo", mapValue = double 1.0 }
                  , { mapKey = "config-server-nginx", mapValue = double 1.0 }
                  ]
                }]
            )
  }
}
