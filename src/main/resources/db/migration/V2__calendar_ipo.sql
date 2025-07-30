-- Table: finnhub.calendar_ipo

-- DROP TABLE IF EXISTS finnhub.calendar_ipo;

CREATE TABLE IF NOT EXISTS finnhub.calendar_ipo
(
  date               date,
  exchange           text COLLATE pg_catalog."default",
  name               text COLLATE pg_catalog."default",
  number_of_shares   integer,
  price              text COLLATE pg_catalog."default",
  status             text COLLATE pg_catalog."default",
  symbol             text COLLATE pg_catalog."default",
  total_shares_value bigint,
  CONSTRAINT all_fields_unique UNIQUE NULLS NOT DISTINCT
    (date, exchange, name, number_of_shares, price, status, symbol, total_shares_value)
) TABLESPACE pg_default;

ALTER TABLE IF EXISTS finnhub.calendar_ipo
  OWNER to vertx_demo_dev_user;

--

CREATE MATERIALIZED VIEW finnhub.calendar_ipo_parsed AS
SELECT date,
       exchange,
       name,
       number_of_shares,
       price,
       status,
       symbol,
       total_shares_value,
       CASE
         WHEN price ~ '^\d+(\.\d+)?$' THEN price::numeric
         ELSE NULL END AS price_number,
       CASE
         WHEN price ~ '^\d+(\.\d+)?\s*-\s*\d+(\.\d+)?$' THEN split_part(price, '-', 1)::numeric
         ELSE NULL END AS price_from,
       CASE
         WHEN price ~ '^\d+(\.\d+)?\s*-\s*\d+(\.\d+)?$' THEN split_part(price, '-', 2)::numeric
         ELSE NULL END AS price_to
FROM finnhub.calendar_ipo;
