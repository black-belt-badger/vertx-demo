-- Table: finnhub.news_forex

-- DROP TABLE IF EXISTS finnhub.news_forex;

CREATE TABLE IF NOT EXISTS finnhub.news_forex
(
  category text COLLATE pg_catalog."default",
  datetime integer,
  headline text COLLATE pg_catalog."default",
  id       integer,
  image    text COLLATE pg_catalog."default",
  related  text COLLATE pg_catalog."default",
  source   text COLLATE pg_catalog."default",
  summary  text COLLATE pg_catalog."default",
  url      text COLLATE pg_catalog."default"
) TABLESPACE pg_default;

ALTER TABLE IF EXISTS finnhub.news_forex
  OWNER to vertx_demo_dev_user;

-- indexes

CREATE INDEX idx_news_forex_datetime_desc ON finnhub.news_forex (datetime DESC);
CREATE UNIQUE INDEX idx_news_forex_id ON finnhub.news_forex (id);

-- materialized view

CREATE MATERIALIZED VIEW finnhub.news_forex_view AS
SELECT category,
       to_timestamp(datetime) AT TIME ZONE 'UTC' AS datetime,
       headline,
       id,
       image,
       related,
       source,
       summary,
       url
FROM finnhub.news_forex;

-- materialized view indexes

CREATE INDEX idx_news_forex_view_datetime ON finnhub.news_forex_view (datetime DESC);
