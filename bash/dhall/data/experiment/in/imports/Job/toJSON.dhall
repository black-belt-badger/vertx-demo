let Prelude = ../Prelude.dhall

let JSON = Prelude.JSON

let Map = Prelude.Map

let Job = ./Type.dhall

let dropNones = ../dropNones.dhall

let Optional/map = Prelude.Optional.map

in  let Job/toJSON
        : Job -> JSON.Type
        = \(job : Job) ->
            let everything
                : Map.Type Text (Optional JSON.Type)
                = toMap
                    { stage = Optional/map Text JSON.Type JSON.string job.stage
                    , name = Optional/map Text JSON.Type JSON.string job.name
                    }

            in  JSON.object (dropNones Text JSON.Type everything)

    in  Job/toJSON
