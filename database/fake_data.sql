-- ============================================================
-- TradeX Extra Seed Data
-- Run AFTER schema.sql and data.sql (or via init.sh)
-- Adds extra accounts with holdings and watchlist entries.
-- All passwords hash to: password
-- ============================================================

USE TradeX;

INSERT IGNORE INTO account (account_number, login_password, created_date, cash_balance, market_value, email, role, status)
VALUES
  (2147483640, '$2a$10$sv15MyIE8z1xWmFrOOpUZ.yc63SA6YkxDPgesAR8Fgsot1b5Tycma', '2025-03-01', 75000.000000, 22000.000000, 'alice.wong@email.com',  'Regular', 'Active'),
  (2147483639, '$2a$10$sv15MyIE8z1xWmFrOOpUZ.yc63SA6YkxDPgesAR8Fgsot1b5Tycma', '2025-03-05', 30000.000000,  9500.000000, 'bob.martin@email.com',  'Regular', 'Active'),
  (2147483638, '$2a$10$sv15MyIE8z1xWmFrOOpUZ.yc63SA6YkxDPgesAR8Fgsot1b5Tycma', '2025-03-10', 10000.000000,  4200.000000, 'carol.james@email.com', 'Regular', 'Active');

INSERT IGNORE INTO `user` (id, email, phone_number, account_number, first_name, last_name, birth_date, reason_for_signup)
VALUES
  (4, 'alice.wong@email.com',  '555-201-0001', 2147483640, 'Alice', 'Wong',   '1993-07-14', 'Portfolio growth'),
  (5, 'bob.martin@email.com',  '555-201-0002', 2147483639, 'Bob',   'Martin', '1990-11-30', 'Retirement savings'),
  (6, 'carol.james@email.com', '555-201-0003', 2147483638, 'Carol', 'James',  '2000-04-05', 'Learning to invest');

INSERT IGNORE INTO `order` (id, account_number, symbol, unit_price, quantity, action, created_time, status)
VALUES
  ('aA1bB2cC3d', 2147483640, 'NVDA', 875.500000, 10.000000, 'Buy', '2025-03-02 09:30:00', 'Filled'),
  ('eE4fF5gG6h', 2147483640, 'META', 510.200000,  8.000000, 'Buy', '2025-03-03 10:15:00', 'Filled'),
  ('iI7jJ8kK9l', 2147483639, 'AMZN', 185.300000, 20.000000, 'Buy', '2025-03-06 11:00:00', 'Filled'),
  ('mM0nN1oO2p', 2147483639, 'NFLX', 620.750000,  5.000000, 'Buy', '2025-03-07 13:45:00', 'Filled'),
  ('qQ3rR4sS5t', 2147483638, 'TSLA', 260.000000,  6.000000, 'Buy', '2025-03-11 08:50:00', 'Filled'),
  ('uU6vV7wW8x', 2147483638, 'AMD',  175.400000, 12.000000, 'Buy', '2025-03-12 14:20:00', 'Filled');

INSERT IGNORE INTO holding (account_number, symbol, unit_price, quantity)
VALUES
  (2147483640, 'NVDA', 875.500000, 10.000000),
  (2147483640, 'META', 510.200000,  8.000000),
  (2147483639, 'AMZN', 185.300000, 20.000000),
  (2147483639, 'NFLX', 620.750000,  5.000000),
  (2147483638, 'TSLA', 260.000000,  6.000000),
  (2147483638, 'AMD',  175.400000, 12.000000);

INSERT IGNORE INTO watching (account_number, symbol)
VALUES
  (2147483640, 'AAPL'),
  (2147483640, 'MSFT'),
  (2147483640, 'GOOGL'),
  (2147483639, 'NVDA'),
  (2147483639, 'TSLA'),
  (2147483638, 'META'),
  (2147483638, 'AMZN');
