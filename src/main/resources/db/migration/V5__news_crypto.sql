-- Table: finnhub.news_crypto

-- DROP TABLE IF EXISTS finnhub.news_crypto;

CREATE TABLE IF NOT EXISTS finnhub.news_crypto
(
  category text COLLATE pg_catalog."default",
  datetime integer,
  headline text COLLATE pg_catalog."default",
  id       integer,
  image    text COLLATE pg_catalog."default",
  related  text COLLATE pg_catalog."default",
  source   text COLLATE pg_catalog."default",
  summary  text COLLATE pg_catalog."default",
  url      text COLLATE pg_catalog."default",
  CONSTRAINT news_crypto_unique_id UNIQUE (id)
) TABLESPACE pg_default;

ALTER TABLE IF EXISTS finnhub.news_crypto
  OWNER to vertx_demo_dev_user;

-- indexes

CREATE INDEX idx_news_crypto_datetime_desc ON finnhub.news_crypto (datetime DESC);
CREATE UNIQUE INDEX idx_news_crypto_id ON finnhub.news_crypto (id);

-- materialized view

CREATE MATERIALIZED VIEW finnhub.news_crypto_view AS
SELECT category,
       to_timestamp(datetime) AT TIME ZONE 'UTC' AS datetime,
       headline,
       id,
       image,
       related,
       source,
       summary,
       url
FROM finnhub.news_crypto;

-- materialized view indexes

CREATE INDEX idx_news_crypto_view_datetime ON finnhub.news_crypto_view (datetime DESC);
