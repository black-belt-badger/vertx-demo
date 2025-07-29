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

-- News record
data News = News
  { category :: Maybe T.Text
  , datetime :: Maybe Natural
  , headline :: Maybe T.Text
  , nid :: Maybe Natural
  , image :: Maybe T.Text
  , related :: Maybe T.Text
  , source :: Maybe T.Text
  , summary :: Maybe T.Text
  , url :: Maybe T.Text
  } deriving (Show)

instance Dhall.FromDhall News where
  autoWith _ = Dhall.record
    ( News
      <$> Dhall.field "category" (Dhall.maybe Dhall.strictText)
      <*> Dhall.field "datetime" (Dhall.maybe Dhall.natural)
      <*> Dhall.field "headline" (Dhall.maybe Dhall.strictText)
      <*> Dhall.field "id" (Dhall.maybe Dhall.natural)
      <*> Dhall.field "image" (Dhall.maybe Dhall.strictText)
      <*> Dhall.field "related" (Dhall.maybe Dhall.strictText)
      <*> Dhall.field "source" (Dhall.maybe Dhall.strictText)
      <*> Dhall.field "summary" (Dhall.maybe Dhall.strictText)
      <*> Dhall.field "url" (Dhall.maybe Dhall.strictText)
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

renderNews :: News -> T.Text
renderNews news =
  "(" <> T.intercalate ", "
    [ toSQLValue (category news)
    , toSQLInt (datetime news)
    , toSQLValue (headline news)
    , toSQLInt (nid news)
    , toSQLValue (image news)
    ,  "'" <> toSQLValue (related news) <> "'"
    ,  toSQLValue (source news)
    ,  toSQLValue (summary news)
    ,  toSQLValue (url news)
    ] <> ")"

main :: IO ()
main = do
  args <- getArgs
  case args of
    ["calendar_ipo", inputPath, outputPath] -> do
      ipos <- Dhall.input (Dhall.auto :: Dhall.Decoder [IPO]) (T.pack inputPath)
      let header = "INSERT INTO finnhub.calendar_ipo (date, exchange, name, number_of_shares, price, status, symbol, total_shares_value) VALUES"
          values = T.intercalate ",\n" (map renderIPO ipos)
          statement = T.unlines [header, values, "ON CONFLICT ON CONSTRAINT value_difference DO NOTHING;"]
      TIO.writeFile outputPath statement
      putStrLn $ "IPO SQL written to " ++ outputPath

    ["news_general", inputPath, outputPath] -> do
      newsItems <- Dhall.input (Dhall.auto :: Dhall.Decoder [News]) (T.pack inputPath)
      let header = "INSERT INTO finnhub.news_general (category, datetime, headline, id, image, related, source, summary, url) VALUES"
          values = T.intercalate ",\n" (map renderNews newsItems)
          statement = T.unlines [header, values, "ON CONFLICT DO NOTHING;"]
      TIO.writeFile outputPath statement
      putStrLn $ "News SQL written to " ++ outputPath

    _ -> die "Usage: dhall-to-sql <calendar_ipo|news_general> <input.dhall> <output.sql>"
