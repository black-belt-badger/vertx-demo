let package = ./imports/Job/package.dhall

let type = ./imports/Job/Type.dhall

let default = ./imports/Job/default.dhall

let job1
    : type
    = { stage = Some "preliminary", name = Some "guess" }

let job2
    : type
    = { stage = None Text, name = Some "guess" }

let job3
    : type
    = { stage = Some "preliminary", name = None Text }

let job4
    : type
    = { stage = None Text, name = None Text }

in  { job1, job2, job3, job4 }
