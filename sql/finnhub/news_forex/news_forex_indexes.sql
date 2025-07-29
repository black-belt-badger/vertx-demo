CREATE INDEX idx_news_forex_datetime_desc
  ON finnhub.news_forex(datetime DESC);

CREATE UNIQUE INDEX idx_news_forex_id
  ON finnhub.news_forex(id);
