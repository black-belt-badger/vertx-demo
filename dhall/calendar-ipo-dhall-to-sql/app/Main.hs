{-# LANGUAGE OverloadedStrings #-}

module Main where

import qualified Dhall
import qualified Data.Text as T
import qualified Data.Text.IO as TIO
import Data.Maybe (fromMaybe)
import System.Environment (getArgs)
import System.Exit (die)

import Numeric.Natural (Natural)

-- IPO record
data IPO = IPO
  { date :: T.Text
  , exchange :: Maybe T.Text
  , name :: T.Text
  , numberOfShares :: Maybe Natural
  , price :: Maybe T.Text
  , status :: T.Text
  , symbol :: Maybe T.Text
  , totalSharesValue :: Maybe Natural
  } deriving (Show)

instance Dhall.FromDhall IPO where
  autoWith _ = Dhall.record
    ( IPO
      <$> Dhall.field "date" Dhall.strictText
      <*> Dhall.field "exchange" (Dhall.maybe Dhall.strictText)
      <*> Dhall.field "name" Dhall.strictText
      <*> Dhall.field "numberOfShares" (Dhall.maybe Dhall.natural)
      <*> Dhall.field "price" (Dhall.maybe Dhall.strictText)
      <*> Dhall.field "status" Dhall.strictText
      <*> Dhall.field "symbol" (Dhall.maybe Dhall.strictText)
      <*> Dhall.field "totalSharesValue" (Dhall.maybe Dhall.natural)
    )

escapeText :: T.Text -> T.Text
escapeText = T.replace "'" "''"

toSQLValue :: Maybe T.Text -> T.Text
toSQLValue Nothing = "NULL"
toSQLValue (Just txt) = "'" <> escapeText txt <> "'"

toSQLInt :: Maybe Natural -> T.Text
toSQLInt Nothing = "NULL"
toSQLInt (Just i) = T.pack (show i)

renderIPO :: IPO -> T.Text
renderIPO ipo =
  "(" <> T.intercalate ", "
    [ "'" <> escapeText (date ipo) <> "'"
    , toSQLValue (exchange ipo)
    , "'" <> escapeText (name ipo) <> "'"
    , toSQLInt (numberOfShares ipo)
    , toSQLValue (price ipo)
    , "'" <> escapeText (status ipo) <> "'"
    , toSQLValue (symbol ipo)
    , toSQLInt (totalSharesValue ipo)
    ] <> ")"

main :: IO ()
main = do
  args <- getArgs
  case args of
    [inputPath, outputPath] -> do
      ipos <- Dhall.input (Dhall.auto :: Dhall.Decoder [IPO]) (T.pack inputPath)
      let header = "INSERT INTO fh.calendar_ipo (date, exchange, name, number_of_shares, price, status, symbol, total_shares_value) VALUES"
          values = T.intercalate ",\n" (map renderIPO ipos)
          statement = T.unlines [header, values, "ON CONFLICT ON CONSTRAINT value_difference DO NOTHING;"]
      TIO.writeFile outputPath statement
      putStrLn $ "SQL written to " ++ outputPath
    _ -> die "Usage: dhall-to-sql <input.dhall> <output.sql>"
