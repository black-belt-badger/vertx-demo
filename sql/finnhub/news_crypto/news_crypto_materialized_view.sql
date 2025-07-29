CREATE MATERIALIZED VIEW finnhub.news_crypto_view AS
SELECT
  category,
  to_timestamp(datetime) AT TIME ZONE 'UTC' AS datetime,
  headline,
  id,
  image,
  related,
  source,
  summary,
  url
FROM
  finnhub.news_crypto;
