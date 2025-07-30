-- Table: finnhub.news_merger

-- DROP TABLE IF EXISTS finnhub.news_merger;

CREATE TABLE IF NOT EXISTS finnhub.news_merger
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

ALTER TABLE IF EXISTS finnhub.news_merger
  OWNER to vertx_demo_dev_user;

-- indexes

CREATE INDEX idx_news_merger_datetime_desc ON finnhub.news_merger (datetime DESC);
CREATE UNIQUE INDEX idx_news_merger_id ON finnhub.news_merger (id);

-- materialized view

CREATE MATERIALIZED VIEW finnhub.news_merger_view AS
SELECT category,
       to_timestamp(datetime) AT TIME ZONE 'UTC' AS datetime,
       headline,
       id,
       image,
       related,
       source,
       summary,
       url
FROM finnhub.news_merger;

-- materialized view indexes

CREATE INDEX idx_news_merger_view_datetime ON finnhub.news_merger_view (datetime DESC);
