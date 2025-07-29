CREATE INDEX idx_news_general_datetime_desc
  ON finnhub.news_general(datetime DESC);

CREATE UNIQUE INDEX idx_news_general_id
  ON finnhub.news_general(id);
