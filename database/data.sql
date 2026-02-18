-- ============================================================
-- TradeX Sample Data
-- MySQL Database
-- ============================================================

USE TradeX;

-- ============================================================
-- 1. ACCOUNTS (insert before USERS due to FK dependency)
-- ============================================================
INSERT INTO ACCOUNTS (account_number, login_password, created_date, cash_balance, market_value,
    linked_account_institution_name, linked_account_number, email, role, status)
VALUES
    (2147483647, 'admin123', '2025-01-01', 100000.000000, 0.000000, NULL, NULL, 'admin@tradex.com', 'Admin', 'Active'),
    (2147483646, 'password123', '2025-01-15', 50000.000000, 12500.000000, 'Chase Bank', 123456, 'john.doe@email.com', 'Regular', 'Active'),
    (2147483645, 'password456', '2025-02-01', 25000.000000, 8750.000000, 'Bank of America', 789012, 'jane.smith@email.com', 'Regular', 'Active');

-- ============================================================
-- 2. USERS
-- ============================================================
INSERT INTO USERS (id, email, phone_number, account_number, first_name, middle_name, last_name, birth_date, reason_for_signup)
VALUES
    (1, 'admin@tradex.com', '555-000-0000', 2147483647, 'System', NULL, 'Admin', '1990-01-01', 'System administrator'),
    (2, 'john.doe@email.com', '555-123-4567', 2147483646, 'John', 'Michael', 'Doe', '1995-06-15', 'Long-term investing'),
    (3, 'jane.smith@email.com', '555-987-6543', 2147483645, 'Jane', NULL, 'Smith', '1998-03-22', 'Day trading');

-- ============================================================
-- 3. INSTRUMENTS
-- ============================================================
INSERT INTO instruments (symbol, long_name, short_name, display_name, quote_type, type_disp,
    currency, exchange, full_exchange_name, market, market_state, quote_source_name,
    triggerable, custom_price_alert_confidence, has_pre_post_market_data,
    esg_populated, tradeable, crypto_tradeable, sector, industry, company_description)
VALUES
    ('AAPL', 'Apple Inc.', 'Apple Inc.', 'Apple', 'EQUITY', 'Equity', 'USD', 'NMS', 'NasdaqGS', 'us_market', 'REGULAR', 'Nasdaq Real Time Price',
     TRUE, 'HIGH', TRUE, TRUE, TRUE, FALSE, 'Technology', 'Consumer Electronics',
     'Apple Inc. designs, manufactures, and markets smartphones, personal computers, tablets, wearables, and accessories worldwide.'),

    ('MSFT', 'Microsoft Corporation', 'Microsoft Corporation', 'Microsoft', 'EQUITY', 'Equity', 'USD', 'NMS', 'NasdaqGS', 'us_market', 'REGULAR', 'Nasdaq Real Time Price',
     TRUE, 'HIGH', TRUE, TRUE, TRUE, FALSE, 'Technology', 'Software - Infrastructure',
     'Microsoft Corporation develops and supports software, services, devices, and solutions worldwide.'),

    ('GOOGL', 'Alphabet Inc.', 'Alphabet Inc.', 'Alphabet', 'EQUITY', 'Equity', 'USD', 'NMS', 'NasdaqGS', 'us_market', 'REGULAR', 'Nasdaq Real Time Price',
     TRUE, 'HIGH', TRUE, TRUE, TRUE, FALSE, 'Technology', 'Internet Content & Information',
     'Alphabet Inc. offers various products and platforms in the United States, Europe, the Middle East, Africa, the Asia-Pacific, Canada, and Latin America.'),

    ('AMZN', 'Amazon.com, Inc.', 'Amazon.com, Inc.', 'Amazon', 'EQUITY', 'Equity', 'USD', 'NMS', 'NasdaqGS', 'us_market', 'REGULAR', 'Nasdaq Real Time Price',
     TRUE, 'HIGH', TRUE, TRUE, TRUE, FALSE, 'Consumer Cyclical', 'Internet Retail',
     'Amazon.com, Inc. engages in the retail sale of consumer products, advertising, and subscription services through online and physical stores.'),

    ('TSLA', 'Tesla, Inc.', 'Tesla, Inc.', 'Tesla', 'EQUITY', 'Equity', 'USD', 'NMS', 'NasdaqGS', 'us_market', 'REGULAR', 'Nasdaq Real Time Price',
     TRUE, 'HIGH', TRUE, TRUE, TRUE, FALSE, 'Consumer Cyclical', 'Auto Manufacturers',
     'Tesla, Inc. designs, develops, manufactures, leases, and sells electric vehicles, and energy generation and storage systems.'),

    ('^GSPC', 'S&P 500', 'S&P 500', 'S&P 500', 'INDEX', 'Index', 'USD', 'SNP', 'SNP', 'us_market', 'REGULAR', 'Delayed Quote',
     FALSE, NULL, FALSE, FALSE, FALSE, FALSE, NULL, NULL, NULL),

    ('^DJI', 'Dow Jones Industrial Average', 'Dow 30', 'Dow Jones', 'INDEX', 'Index', 'USD', 'DJI', 'DJI', 'us_market', 'REGULAR', 'Delayed Quote',
     FALSE, NULL, FALSE, FALSE, FALSE, FALSE, NULL, NULL, NULL),

    ('^IXIC', 'NASDAQ Composite', 'NASDAQ Composite', 'NASDAQ', 'INDEX', 'Index', 'USD', 'NIM', 'NASDAQ', 'us_market', 'REGULAR', 'Delayed Quote',
     FALSE, NULL, FALSE, FALSE, FALSE, FALSE, NULL, NULL, NULL);

-- ============================================================
-- 4. MARKET_DATA
-- ============================================================
INSERT INTO market_data (symbol, regular_market_price, regular_market_change, regular_market_change_percent,
    regular_market_open, regular_market_day_high, regular_market_day_low, regular_market_previous_close,
    regular_market_volume, post_market_price, post_market_change, post_market_change_percent,
    fifty_two_week_low, fifty_two_week_high,
    fifty_day_average, fifty_day_average_change, fifty_day_average_change_percent,
    two_hundred_day_average, two_hundred_day_average_change, two_hundred_day_average_change_percent,
    average_daily_volume_3m, average_daily_volume_10d)
VALUES
    ('AAPL', 227.630000, 1.250000, 0.552000, 226.010000, 228.340000, 225.800000, 226.380000,
     48230000, 227.800000, 0.170000, 0.075000, 164.080000, 237.490000,
     224.500000, 3.130000, 1.394000, 198.750000, 28.880000, 14.530000,
     54320000, 47850000),

    ('MSFT', 415.560000, 2.340000, 0.566000, 413.200000, 416.890000, 412.100000, 413.220000,
     22150000, 415.780000, 0.220000, 0.053000, 309.450000, 430.820000,
     410.200000, 5.360000, 1.306000, 385.600000, 29.960000, 7.768000,
     25680000, 21340000),

    ('GOOGL', 174.250000, -0.870000, -0.497000, 175.100000, 176.200000, 173.500000, 175.120000,
     28450000, 174.400000, 0.150000, 0.086000, 130.670000, 191.750000,
     170.800000, 3.450000, 2.020000, 158.300000, 15.950000, 10.075000,
     30120000, 27650000),

    ('AMZN', 198.430000, 3.210000, 1.643000, 195.800000, 199.500000, 195.100000, 195.220000,
     52340000, 198.600000, 0.170000, 0.086000, 151.610000, 201.200000,
     190.500000, 7.930000, 4.163000, 180.200000, 18.230000, 10.117000,
     58670000, 51200000),

    ('TSLA', 248.420000, -5.780000, -2.273000, 253.100000, 255.800000, 246.300000, 254.200000,
     95670000, 249.100000, 0.680000, 0.274000, 138.800000, 278.980000,
     240.300000, 8.120000, 3.379000, 215.600000, 32.820000, 15.223000,
     102340000, 89560000),

    ('^GSPC', 5942.470000, 23.450000, 0.396000, 5920.100000, 5955.200000, 5915.300000, 5919.020000,
     3250000000, 0.000000, 0.000000, 0.000000, 4953.560000, 5958.200000,
     5850.300000, 92.170000, 1.575000, 5620.400000, 322.070000, 5.731000,
     3850000000, 3420000000),

    ('^DJI', 43840.910000, 178.250000, 0.408000, 43700.500000, 43920.100000, 43650.200000, 43662.660000,
     385000000, 0.000000, 0.000000, 0.000000, 37611.560000, 44000.250000,
     43200.500000, 640.410000, 1.482000, 41500.300000, 2340.610000, 5.639000,
     420000000, 375000000),

    ('^IXIC', 19012.320000, 92.180000, 0.487000, 18940.500000, 19050.100000, 18900.200000, 18920.140000,
     5120000000, 0.000000, 0.000000, 0.000000, 15282.010000, 19100.500000,
     18700.400000, 311.920000, 1.668000, 17800.500000, 1211.820000, 6.808000,
     5680000000, 5120000000);

-- ============================================================
-- 5. FUNDAMENTAL_METRICS
-- ============================================================
INSERT INTO fundamental_metrics (symbol, market_cap, trailing_pe, forward_pe, price_to_book,
    trailing_eps, forward_eps, eps_current_year, price_eps_current_year,
    trailing_annual_dividend_rate, trailing_annual_dividend_yield,
    dividend_rate, dividend_yield, shares_outstanding, book_value, average_analyst_rating,
    earnings_timestamp, earnings_timestamp_start, dividend_date)
VALUES
    ('AAPL', 3480000000000, 37.520000, 33.180000, 56.230000,
     6.070000, 6.860000, 7.100000, 32.060000,
     0.960000, 0.422000, 1.000000, 0.439000, 15290000000, 4.050000, '1.8 - Buy',
     '2025-04-24', '2025-04-24', '2025-02-13'),

    ('MSFT', 3090000000000, 35.840000, 30.120000, 12.560000,
     11.600000, 13.790000, 13.200000, 31.480000,
     3.000000, 0.722000, 3.320000, 0.799000, 7430000000, 33.090000, '1.5 - Buy',
     '2025-04-22', '2025-04-22', '2025-03-13'),

    ('GOOGL', 2140000000000, 23.150000, 20.280000, 7.120000,
     7.530000, 8.590000, 8.800000, 19.800000,
     0.800000, 0.459000, 0.800000, 0.459000, 12280000000, 24.480000, '1.7 - Buy',
     '2025-04-24', '2025-04-24', '2025-03-17'),

    ('AMZN', 2080000000000, 38.920000, 28.640000, 8.450000,
     5.100000, 6.930000, 6.500000, 30.530000,
     0.000000, 0.000000, 0.000000, 0.000000, 10480000000, 23.490000, '1.3 - Buy',
     '2025-04-24', '2025-04-24', NULL),

    ('TSLA', 795000000000, 68.450000, 95.230000, 15.780000,
     3.630000, 2.610000, 3.200000, 77.630000,
     0.000000, 0.000000, 0.000000, 0.000000, 3200000000, 15.740000, '2.8 - Hold',
     '2025-04-22', '2025-04-22', NULL);

-- ============================================================
-- 6. HISTORICAL_QUOTES (sample: last 5 days for AAPL)
-- ============================================================
INSERT INTO historical_quotes (symbol, date, open, high, low, close, volume)
VALUES
    ('AAPL', '2025-02-10 00:00:00', 224.500000, 226.800000, 223.100000, 225.900000, 51230000),
    ('AAPL', '2025-02-11 00:00:00', 225.900000, 227.200000, 224.800000, 226.380000, 48560000),
    ('AAPL', '2025-02-12 00:00:00', 226.400000, 228.100000, 225.500000, 227.100000, 45670000),
    ('AAPL', '2025-02-13 00:00:00', 227.100000, 228.500000, 226.200000, 226.010000, 47890000),
    ('AAPL', '2025-02-14 00:00:00', 226.010000, 228.340000, 225.800000, 227.630000, 48230000),

    ('MSFT', '2025-02-10 00:00:00', 410.200000, 413.500000, 409.100000, 412.300000, 23450000),
    ('MSFT', '2025-02-11 00:00:00', 412.300000, 414.100000, 411.200000, 413.220000, 22100000),
    ('MSFT', '2025-02-12 00:00:00', 413.300000, 415.700000, 412.500000, 414.800000, 21340000),
    ('MSFT', '2025-02-13 00:00:00', 414.800000, 416.200000, 413.600000, 413.200000, 22560000),
    ('MSFT', '2025-02-14 00:00:00', 413.200000, 416.890000, 412.100000, 415.560000, 22150000);

-- ============================================================
-- 7. ORDERS
-- ============================================================
INSERT INTO orders (id, account_number, symbol, unit_price, quantity, action, created_time, status)
VALUES
    ('aBcDeFgHiJ', 2147483646, 'AAPL', 225.900000, 20.000000, 'Buy', '2025-02-10 10:30:00', 'Filled'),
    ('kLmNoPqRsT', 2147483646, 'MSFT', 412.300000, 10.000000, 'Buy', '2025-02-10 11:15:00', 'Filled'),
    ('uVwXyZ1234', 2147483645, 'TSLA', 253.100000, 15.000000, 'Buy', '2025-02-12 09:45:00', 'Filled'),
    ('5678AbCdEf', 2147483645, 'GOOGL', 175.100000, 25.000000, 'Buy', '2025-02-13 14:20:00', 'Filled'),
    ('GhIjKlMnOp', 2147483646, 'AAPL', 227.100000, 5.000000, 'Sell', '2025-02-13 15:30:00', 'Filled');

-- ============================================================
-- 8. ACCOUNT_HOLDS_SYMBOLS
-- ============================================================
INSERT INTO ACCOUNT_HOLDS_SYMBOLS (account_number, symbol, unit_price, quantity)
VALUES
    (2147483646, 'AAPL', 225.900000, 15.000000),
    (2147483646, 'MSFT', 412.300000, 10.000000),
    (2147483645, 'TSLA', 253.100000, 15.000000),
    (2147483645, 'GOOGL', 175.100000, 25.000000);

-- ============================================================
-- 9. ACCOUNT_WATCHES_SYMBOLS
-- ============================================================
INSERT INTO ACCOUNT_WATCHES_SYMBOLS (account_number, symbol)
VALUES
    (2147483646, 'TSLA'),
    (2147483646, 'AMZN'),
    (2147483646, 'GOOGL'),
    (2147483645, 'AAPL'),
    (2147483645, 'MSFT'),
    (2147483645, 'AMZN');
