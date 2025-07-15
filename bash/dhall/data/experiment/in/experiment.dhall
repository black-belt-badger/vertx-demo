let package = ./imports/Job/package.dhall

let type = ./imports/Job/Type.dhall

let default = ./imports/Job/default.dhall

let job
    : type
    = { stage = Some "preliminary", name = Some "guess" }

in  job
