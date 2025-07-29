-- Table: finnhub.calendar_ipo

-- DROP TABLE IF EXISTS finnhub.calendar_ipo;

CREATE TABLE IF NOT EXISTS finnhub.calendar_ipo
(
  date date,
  exchange text COLLATE pg_catalog."default",
  name text COLLATE pg_catalog."default",
  number_of_shares integer,
  price text COLLATE pg_catalog."default",
  status text COLLATE pg_catalog."default",
  symbol text COLLATE pg_catalog."default",
  total_shares_value bigint,
  CONSTRAINT all_fields_unique UNIQUE (date, exchange, name, number_of_shares, price, status, symbol, total_shares_value)
)

  TABLESPACE pg_default;

ALTER TABLE IF EXISTS finnhub.calendar_ipo
  OWNER to vertx_demo_dev_user;
