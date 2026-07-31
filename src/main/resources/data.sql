-- Seed data. Runs on every startup because the database is in-memory and
-- rebuilt from scratch each time.

INSERT INTO experience (title, city, description, price, currency, duration_minutes, daily_capacity, family_friendly) VALUES
('Skip-the-Line Colosseum Tour', 'Rome', 'Guided walk through the Colosseum, Roman Forum and Palatine Hill.', 54.00, 'EUR', 180, 30, true),
('Sunset Kayak on the Douro', 'Porto', 'Two hours on the water with a guide, finishing under the Dom Luis bridge.', 38.50, 'EUR', 120, 12, false),
('Pasta Making with a Nonna', 'Rome', 'Small-group cooking class in a family kitchen in Trastevere.', 79.00, 'EUR', 210, 8, true),
('Oxford Colleges Walking Tour', 'Oxford', 'Ninety minutes through Christ Church, Bodleian and Radcliffe Camera.', 25.00, 'GBP', 90, 25, true),
('Punting on the Cherwell', 'Oxford', 'Self-punt hire with a fifteen minute lesson from the boathouse.', 32.00, 'GBP', 60, 10, false),
('Krakow Old Town Food Crawl', 'Krakow', 'Six tastings across the Kazimierz district with a local guide.', 45.00, 'PLN', 150, 16, true),
('Tram Ride and Tile Workshop', 'Lisbon', 'Ride the 28 tram, then paint your own azulejo tile to take home.', 61.00, 'EUR', 165, 14, false);
