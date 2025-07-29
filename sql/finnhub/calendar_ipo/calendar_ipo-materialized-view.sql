CREATE MATERIALIZED VIEW finnhub.calendar_ipo_parsed AS
SELECT
  date,
  exchange,
  name,
  number_of_shares,
  price,
  status,
  symbol,
  total_shares_value,

  -- Parse numeric price when it's a single number
  CASE
    WHEN price ~ '^\d+(\.\d+)?$' THEN price::numeric
    ELSE NULL
    END AS price_number,

  -- Parse price range: from
  CASE
    WHEN price ~ '^\d+(\.\d+)?\s*-\s*\d+(\.\d+)?$'
      THEN split_part(price, '-', 1)::numeric
    ELSE NULL
    END AS price_from,

  -- Parse price range: to
  CASE
    WHEN price ~ '^\d+(\.\d+)?\s*-\s*\d+(\.\d+)?$'
      THEN split_part(price, '-', 2)::numeric
    ELSE NULL
    END AS price_to

FROM finnhub.calendar_ipo;
