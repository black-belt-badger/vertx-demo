--let Prelude = https://prelude.dhall-lang.org/v23.0.0/package.dhall
--let Prelude =
--      https://prelude.dhall-lang.org/package.dhall sha256:931cbfae9d746c4611b07633ab1e547637ab4ba138b16bf65ef1b9ad66a60b7f

let Prelude =
        env:DHALL_PRELUDE
      ? /usr/share/dhall/Prelude
      ? https://prelude.dhall-lang.org/v23.0.0/package.dhall


let JSON = Prelude.JSON

let Job = ./Type.dhall

let Job/toJSON
      : Job -> JSON.Type
      = \(job: Job) ->
        JSON.string "this is my string"

in Job/toJSON
