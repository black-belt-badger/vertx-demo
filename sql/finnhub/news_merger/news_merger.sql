-- Table: finnhub.news_merger

-- DROP TABLE IF EXISTS finnhub.news_merger;

CREATE TABLE IF NOT EXISTS finnhub.news_merger
(
  category text COLLATE pg_catalog."default",
  datetime integer,
  headline text COLLATE pg_catalog."default",
  id integer,
  image text COLLATE pg_catalog."default",
  related text COLLATE pg_catalog."default",
  source text COLLATE pg_catalog."default",
  summary text COLLATE pg_catalog."default",
  url text COLLATE pg_catalog."default"
)

  TABLESPACE pg_default;

ALTER TABLE IF EXISTS finnhub.news_merger
  OWNER to vertx_demo_dev_user;
