CREATE INDEX idx_news_crypto_datetime_desc
  ON finnhub.news_crypto(datetime DESC);

CREATE UNIQUE INDEX idx_news_crypto_id
  ON finnhub.news_crypto(id);
