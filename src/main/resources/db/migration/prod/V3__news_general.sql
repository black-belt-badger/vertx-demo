-- Table: finnhub.news_general

-- DROP TABLE IF EXISTS finnhub.news_general;

CREATE TABLE IF NOT EXISTS finnhub.news_general
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
  CONSTRAINT news_general_unique_id UNIQUE (id)
) TABLESPACE pg_default;

ALTER TABLE IF EXISTS finnhub.news_general
  OWNER to vertx_demo_admin;

-- indexes

CREATE INDEX idx_news_general_datetime_desc ON finnhub.news_general (datetime DESC);
CREATE UNIQUE INDEX idx_news_general_id ON finnhub.news_general (id);

-- materialized view

CREATE MATERIALIZED VIEW finnhub.news_general_view AS
SELECT category,
       to_timestamp(datetime) AT TIME ZONE 'UTC' AS datetime,
       headline,
       id,
       image,
       related,
       source,
       summary,
       url
FROM finnhub.news_general;

-- materialized view indexes

CREATE INDEX idx_news_general_view_datetime ON finnhub.news_general_view (datetime DESC);
