-- ============================================================
-- TradeX Seed Data
-- MySQL Database
-- ============================================================

USE TradeX;

-- ============================================================
-- 1. account
-- ============================================================
INSERT INTO account (account_number, login_password, created_date, cash_balance, market_value,
    linked_account_institution_name, linked_account_number, email, role, status)
VALUES
    (2147483647, '$2a$10$sv15MyIE8z1xWmFrOOpUZ.yc63SA6YkxDPgesAR8Fgsot1b5Tycma', '2025-01-01', 100000.000000, 0.000000, NULL, NULL, 'admin@tradex.com', 'Admin', 'Active'),
    (2147483646, '$2a$10$sv15MyIE8z1xWmFrOOpUZ.yc63SA6YkxDPgesAR8Fgsot1b5Tycma', '2025-01-15', 50000.000000, 12500.000000, 'Chase Bank', 123456, 'john.doe@email.com', 'Regular', 'Active'),
    (2147483645, '$2a$10$sv15MyIE8z1xWmFrOOpUZ.yc63SA6YkxDPgesAR8Fgsot1b5Tycma', '2025-02-01', 25000.000000, 8750.000000, 'Bank of America', 789012, 'jane.smith@email.com', 'Regular', 'Active');

-- ============================================================
-- 2. user
-- ============================================================
INSERT INTO `user` (id, email, phone_number, account_number, first_name, middle_name, last_name, birth_date, reason_for_signup)
VALUES
    (1, 'admin@tradex.com',    '555-000-0000', 2147483647, 'System', NULL,      'Admin', '1990-01-01', 'System administrator'),
    (2, 'john.doe@email.com',  '555-123-4567', 2147483646, 'John',   'Michael', 'Doe',   '1995-06-15', 'Long-term investing'),
    (3, 'jane.smith@email.com','555-987-6543', 2147483645, 'Jane',   NULL,      'Smith', '1998-03-22', 'Day trading');

-- ============================================================
-- 3. order
-- ============================================================
INSERT INTO `order` (id, account_number, symbol, unit_price, quantity, action, created_time, status)
VALUES
    ('aBcDeFgHiJ', 2147483646, 'AAPL', 225.900000, 20.000000, 'Buy',  '2025-02-10 10:30:00', 'Filled'),
    ('kLmNoPqRsT', 2147483646, 'MSFT', 412.300000, 10.000000, 'Buy',  '2025-02-10 11:15:00', 'Filled'),
    ('uVwXyZ1234', 2147483645, 'TSLA', 253.100000, 15.000000, 'Buy',  '2025-02-12 09:45:00', 'Filled'),
    ('5678AbCdEf', 2147483645, 'GOOGL',175.100000, 25.000000, 'Buy',  '2025-02-13 14:20:00', 'Filled'),
    ('GhIjKlMnOp', 2147483646, 'AAPL', 227.100000,  5.000000, 'Sell', '2025-02-13 15:30:00', 'Filled');

-- ============================================================
-- 4. holding
-- ============================================================
INSERT INTO holding (account_number, symbol, unit_price, quantity)
VALUES
    (2147483646, 'AAPL',  225.900000, 15.000000),
    (2147483646, 'MSFT',  412.300000, 10.000000),
    (2147483645, 'TSLA',  253.100000, 15.000000),
    (2147483645, 'GOOGL', 175.100000, 25.000000);

-- ============================================================
-- 5. watching
-- ============================================================
INSERT INTO watching (account_number, symbol)
VALUES
    (2147483646, 'TSLA'),
    (2147483646, 'AMZN'),
    (2147483646, 'GOOGL'),
    (2147483645, 'AAPL'),
    (2147483645, 'MSFT'),
    (2147483645, 'AMZN');
