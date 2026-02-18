-- ============================================================
-- TradeX Database Schema
-- MySQL Database
-- ============================================================

CREATE DATABASE IF NOT EXISTS TradeX;
USE TradeX;

-- ============================================================
-- 1. ACCOUNTS
-- ============================================================
CREATE TABLE IF NOT EXISTS ACCOUNTS (
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

CREATE INDEX idx_verification_code ON ACCOUNTS(verification_code);
CREATE INDEX idx_email ON ACCOUNTS(email);

-- ============================================================
-- 2. USERS
-- ============================================================
CREATE TABLE IF NOT EXISTS USERS (
    id                  INT             PRIMARY KEY,
    email               VARCHAR(255)    NOT NULL,
    phone_number        VARCHAR(20),
    account_number      INT             NOT NULL UNIQUE,
    first_name          VARCHAR(100)    NOT NULL,
    middle_name         VARCHAR(100),
    last_name           VARCHAR(100)    NOT NULL,
    birth_date          DATE,
    reason_for_signup   VARCHAR(255),

    FOREIGN KEY (account_number) REFERENCES ACCOUNTS(account_number)
        ON DELETE CASCADE
);

-- ============================================================
-- 3. INSTRUMENTS
-- ============================================================
CREATE TABLE IF NOT EXISTS instruments (
    symbol                          VARCHAR(20)     PRIMARY KEY,
    long_name                       VARCHAR(255),
    short_name                      VARCHAR(255),
    display_name                    VARCHAR(255),
    quote_type                      VARCHAR(50),
    type_disp                       VARCHAR(50),
    currency                        VARCHAR(10),
    exchange                        VARCHAR(20),
    full_exchange_name              VARCHAR(100),
    market                          VARCHAR(50),
    market_state                    VARCHAR(20),
    quote_source_name               VARCHAR(100),
    triggerable                     BOOLEAN         DEFAULT FALSE,
    custom_price_alert_confidence   VARCHAR(20),
    has_pre_post_market_data        BOOLEAN         DEFAULT FALSE,
    esg_populated                   BOOLEAN         DEFAULT FALSE,
    tradeable                       BOOLEAN         DEFAULT FALSE,
    crypto_tradeable                BOOLEAN         DEFAULT FALSE,
    sector                          VARCHAR(100),
    industry                        VARCHAR(100),
    company_description             TEXT
);

-- ============================================================
-- 4. MARKET_DATA
-- ============================================================
CREATE TABLE IF NOT EXISTS market_data (
    symbol                              VARCHAR(20)     PRIMARY KEY,
    regular_market_price                DECIMAL(15,6),
    regular_market_change               DECIMAL(15,6),
    regular_market_change_percent       DECIMAL(10,6),
    regular_market_open                 DECIMAL(15,6),
    regular_market_day_high             DECIMAL(15,6),
    regular_market_day_low              DECIMAL(15,6),
    regular_market_previous_close       DECIMAL(15,6),
    regular_market_volume               BIGINT,
    post_market_price                   DECIMAL(15,6),
    post_market_change                  DECIMAL(15,6),
    post_market_change_percent          DECIMAL(10,6),
    fifty_two_week_low                  DECIMAL(15,6),
    fifty_two_week_high                 DECIMAL(15,6),
    fifty_day_average                   DECIMAL(15,6),
    fifty_day_average_change            DECIMAL(15,6),
    fifty_day_average_change_percent    DECIMAL(10,6),
    two_hundred_day_average             DECIMAL(15,6),
    two_hundred_day_average_change      DECIMAL(15,6),
    two_hundred_day_average_change_percent DECIMAL(10,6),
    average_daily_volume_3m             BIGINT,
    average_daily_volume_10d            BIGINT,

    FOREIGN KEY (symbol) REFERENCES instruments(symbol)
        ON DELETE CASCADE
);

-- ============================================================
-- 5. FUNDAMENTAL_METRICS
-- ============================================================
CREATE TABLE IF NOT EXISTS fundamental_metrics (
    symbol                          VARCHAR(20)     PRIMARY KEY,
    market_cap                      BIGINT,
    trailing_pe                     DECIMAL(15,6),
    forward_pe                      DECIMAL(15,6),
    price_to_book                   DECIMAL(15,6),
    trailing_eps                    DECIMAL(15,6),
    forward_eps                     DECIMAL(15,6),
    eps_current_year                DECIMAL(15,6),
    price_eps_current_year          DECIMAL(15,6),
    trailing_annual_dividend_rate   DECIMAL(15,6),
    trailing_annual_dividend_yield  DECIMAL(15,6),
    dividend_rate                   DECIMAL(15,6),
    dividend_yield                  DECIMAL(15,6),
    shares_outstanding              BIGINT,
    book_value                      DECIMAL(15,6),
    average_analyst_rating          VARCHAR(50),
    earnings_timestamp              DATE,
    earnings_timestamp_start        DATE,
    dividend_date                   DATE,

    FOREIGN KEY (symbol) REFERENCES instruments(symbol)
        ON DELETE CASCADE
);

-- ============================================================
-- 6. HISTORICAL_QUOTES
-- ============================================================
CREATE TABLE IF NOT EXISTS historical_quotes (
    symbol      VARCHAR(20)     NOT NULL,
    date        TIMESTAMP       NOT NULL,
    open        DECIMAL(15,6),
    high        DECIMAL(15,6),
    low         DECIMAL(15,6),
    close       DECIMAL(15,6),
    volume      BIGINT,

    PRIMARY KEY (symbol, date),
    FOREIGN KEY (symbol) REFERENCES instruments(symbol)
        ON DELETE CASCADE
);

-- ============================================================
-- 7. ORDERS
-- ============================================================
CREATE TABLE IF NOT EXISTS orders (
    id              VARCHAR(10)     PRIMARY KEY,
    account_number  INT             NOT NULL,
    symbol          VARCHAR(20)     NOT NULL,
    unit_price      DECIMAL(15,6),
    quantity        DECIMAL(15,6),
    action          ENUM('Buy', 'Sell')                 NOT NULL,
    created_time    TIMESTAMP,
    status          ENUM('Open', 'Filled', 'Cancelled') DEFAULT 'Open',

    FOREIGN KEY (account_number) REFERENCES ACCOUNTS(account_number)
        ON DELETE CASCADE,
    FOREIGN KEY (symbol) REFERENCES instruments(symbol)
        ON DELETE CASCADE
);

-- ============================================================
-- 8. ACCOUNT_HOLDS_SYMBOLS
-- ============================================================
CREATE TABLE IF NOT EXISTS ACCOUNT_HOLDS_SYMBOLS (
    account_number  INT             NOT NULL,
    symbol          VARCHAR(20)     NOT NULL,
    unit_price      DECIMAL(15,6),
    quantity        DECIMAL(15,6),

    PRIMARY KEY (account_number, symbol),
    FOREIGN KEY (account_number) REFERENCES ACCOUNTS(account_number)
        ON DELETE CASCADE,
    FOREIGN KEY (symbol) REFERENCES instruments(symbol)
        ON DELETE CASCADE
);

-- ============================================================
-- 9. ACCOUNT_WATCHES_SYMBOLS
-- ============================================================
CREATE TABLE IF NOT EXISTS ACCOUNT_WATCHES_SYMBOLS (
    account_number  INT             NOT NULL,
    symbol          VARCHAR(20)     NOT NULL,

    PRIMARY KEY (account_number, symbol),
    FOREIGN KEY (account_number) REFERENCES ACCOUNTS(account_number)
        ON DELETE CASCADE,
    FOREIGN KEY (symbol) REFERENCES instruments(symbol)
        ON DELETE CASCADE
);
