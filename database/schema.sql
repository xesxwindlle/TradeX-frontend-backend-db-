-- ============================================================
-- TradeX Database Schema
-- MySQL Database
-- ============================================================

CREATE DATABASE IF NOT EXISTS TradeX;
USE TradeX;

-- ============================================================
-- 1. account
-- ============================================================
CREATE TABLE IF NOT EXISTS account (
    account_number          INT             PRIMARY KEY,
    login_password          VARCHAR(255)    NOT NULL,
    created_date            DATE,
    cash_balance            DECIMAL(20,6)   DEFAULT 0,
    market_value            DECIMAL(20,6)   DEFAULT 0,
    linked_account_institution_name VARCHAR(255),
    linked_account_number   INT,
    email                   VARCHAR(255)    NOT NULL UNIQUE,
    role                    ENUM('Regular', 'Admin') DEFAULT 'Regular',
    status                  ENUM('Active', 'Limited', 'Frozen', 'Closed') DEFAULT 'Active',
    verification_code       VARCHAR(10),
    verification_code_expiry TIMESTAMP      NULL
);

CREATE INDEX idx_verification_code ON account(verification_code);
CREATE INDEX idx_email ON account(email);

-- ============================================================
-- 2. user
-- ============================================================
CREATE TABLE IF NOT EXISTS `user` (
    id                  INT             PRIMARY KEY,
    email               VARCHAR(255)    NOT NULL,
    phone_number        VARCHAR(20),
    account_number      INT             NOT NULL UNIQUE,
    first_name          VARCHAR(100)    NOT NULL,
    middle_name         VARCHAR(100),
    last_name           VARCHAR(100)    NOT NULL,
    birth_date          DATE,
    reason_for_signup   VARCHAR(255),

    FOREIGN KEY (account_number) REFERENCES account(account_number)
        ON DELETE CASCADE
);

-- ============================================================
-- 3. order  (backtick-quoted — reserved keyword)
-- ============================================================
CREATE TABLE IF NOT EXISTS `order` (
    id              VARCHAR(10)     PRIMARY KEY,
    account_number  INT             NOT NULL,
    symbol          VARCHAR(20)     NOT NULL,
    unit_price      DECIMAL(15,6),
    quantity        DECIMAL(15,6),
    action          ENUM('Buy', 'Sell')                 NOT NULL,
    created_time    TIMESTAMP,
    status          ENUM('Open', 'Filled', 'Cancelled') DEFAULT 'Open',

    FOREIGN KEY (account_number) REFERENCES account(account_number)
        ON DELETE CASCADE
);

-- ============================================================
-- 4. holding
-- ============================================================
CREATE TABLE IF NOT EXISTS holding (
    account_number  INT             NOT NULL,
    symbol          VARCHAR(20)     NOT NULL,
    unit_price      DECIMAL(15,6),
    quantity        DECIMAL(15,6),

    PRIMARY KEY (account_number, symbol),
    FOREIGN KEY (account_number) REFERENCES account(account_number)
        ON DELETE CASCADE
);

-- ============================================================
-- 5. watching
-- ============================================================
CREATE TABLE IF NOT EXISTS watching (
    account_number  INT             NOT NULL,
    symbol          VARCHAR(20)     NOT NULL,

    PRIMARY KEY (account_number, symbol),
    FOREIGN KEY (account_number) REFERENCES account(account_number)
        ON DELETE CASCADE
);
