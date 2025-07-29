INSERT INTO finnhub.news_general (category, datetime, headline, id, image, related, source, summary, url) VALUES
('top news', 1753745732, 'Jim Cramer explains why Trump''s trade deals didn''t bring on a market rally', 7501407, 'https://image.cnbcfm.com/api/v1/image/103548683-IMG_9928rr.jpg?v=1695684041&w=1920&h=1080', '''', 'CNBC', 'CNBC''s Jim Cramer examined Monday''s market action.', 'https://www.cnbc.com/2025/07/28/jim-cramer-explains-why-trumps-trade-deals-didnt-bring-on-a-market-rally.html')
ON CONFLICT DO NOTHING;
