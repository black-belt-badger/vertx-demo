-- IPO-schema.dhall
let IPO = {
  date : Text,
  exchange : Optional Text,
  name : Text,
  numberOfShares : Optional Natural,
  price : Optional Text,
  status : Text,
  symbol : Optional Text,
  totalSharesValue : Optional Natural
}

in List IPO
