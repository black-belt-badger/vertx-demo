

SELECT DISTINCT ON (exchange, date, symbol) *
FROM finnhub.calendar_ipo_parsed
WHERE exchange IS NULL
   OR exchange IN ('NASDAQ Capital', 'NASDAQ Global', 'NASDAQ Global Select', 'NYSE', 'NYSE MKT')
ORDER BY exchange NULLS LAST, date DESC, symbol;

SELECT *
FROM (
       SELECT *,
              ROW_NUMBER() OVER (PARTITION BY exchange ORDER BY date DESC) AS rn
       FROM finnhub.calendar_ipo_parsed
       WHERE exchange IS NULL
          OR exchange IN ('NASDAQ Capital', 'NASDAQ Global', 'NASDAQ Global Select', 'NYSE', 'NYSE MKT')
     ) sub
WHERE rn <= 3;

SELECT *
FROM (
       SELECT *,
              ROW_NUMBER() OVER (PARTITION BY exchange ORDER BY date DESC) AS rn
       FROM finnhub.calendar_ipo_parsed
     ) sub
WHERE rn <= 3;
