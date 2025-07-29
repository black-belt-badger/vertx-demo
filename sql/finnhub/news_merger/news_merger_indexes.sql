CREATE INDEX idx_news_merger_datetime_desc
  ON finnhub.news_merger(datetime DESC);

CREATE UNIQUE INDEX idx_news_merger_id
  ON finnhub.news_merger(id);
