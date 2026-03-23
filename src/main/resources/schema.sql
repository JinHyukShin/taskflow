-- StockPulse Database Schema

-- Users
CREATE TABLE IF NOT EXISTS app_user (
    id              BIGSERIAL PRIMARY KEY,
    email           VARCHAR(255) NOT NULL UNIQUE,
    password_hash   VARCHAR(255) NOT NULL,
    name            VARCHAR(100) NOT NULL,
    created_at      TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS refresh_token (
    id              BIGSERIAL PRIMARY KEY,
    user_id         BIGINT       NOT NULL REFERENCES app_user(id) ON DELETE CASCADE,
    token           VARCHAR(512) NOT NULL UNIQUE,
    expires_at      TIMESTAMP    NOT NULL,
    created_at      TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_refresh_token_user_id ON refresh_token (user_id);
CREATE INDEX IF NOT EXISTS idx_refresh_token_expires_at ON refresh_token (expires_at);

-- Asset master data
CREATE TABLE IF NOT EXISTS asset (
    id              BIGSERIAL PRIMARY KEY,
    symbol          VARCHAR(20)  NOT NULL UNIQUE,
    name            VARCHAR(200) NOT NULL,
    asset_type      VARCHAR(10)  NOT NULL,
    coingecko_id    VARCHAR(100),
    yahoo_symbol    VARCHAR(20),
    logo_url        VARCHAR(500),
    enabled         BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_asset_symbol ON asset (symbol);
CREATE INDEX IF NOT EXISTS idx_asset_type ON asset (asset_type);

-- Portfolio
CREATE TABLE IF NOT EXISTS portfolio (
    id              BIGSERIAL PRIMARY KEY,
    user_id         BIGINT       NOT NULL REFERENCES app_user(id) ON DELETE CASCADE,
    name            VARCHAR(100) NOT NULL,
    description     VARCHAR(500),
    currency        VARCHAR(5)   NOT NULL DEFAULT 'USD',
    created_at      TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_portfolio_user ON portfolio (user_id);

-- Portfolio assets (holdings)
CREATE TABLE IF NOT EXISTS portfolio_asset (
    id              BIGSERIAL PRIMARY KEY,
    portfolio_id    BIGINT       NOT NULL REFERENCES portfolio(id) ON DELETE CASCADE,
    symbol          VARCHAR(20)  NOT NULL,
    total_quantity  DECIMAL(20, 8) NOT NULL DEFAULT 0,
    avg_buy_price   DECIMAL(20, 8) NOT NULL DEFAULT 0,
    total_invested  DECIMAL(20, 2) NOT NULL DEFAULT 0,
    created_at      TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP    NOT NULL DEFAULT NOW(),
    UNIQUE (portfolio_id, symbol)
);

-- Trade history
CREATE TABLE IF NOT EXISTS trade (
    id              BIGSERIAL PRIMARY KEY,
    portfolio_id    BIGINT       NOT NULL REFERENCES portfolio(id) ON DELETE CASCADE,
    symbol          VARCHAR(20)  NOT NULL,
    trade_type      VARCHAR(4)   NOT NULL,
    quantity        DECIMAL(20, 8) NOT NULL,
    price_per_unit  DECIMAL(20, 8) NOT NULL,
    total_amount    DECIMAL(20, 2) NOT NULL,
    currency        VARCHAR(5)   NOT NULL DEFAULT 'USD',
    fee             DECIMAL(20, 2) DEFAULT 0,
    note            VARCHAR(500),
    traded_at       TIMESTAMP    NOT NULL,
    created_at      TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_trade_portfolio ON trade (portfolio_id, traded_at DESC);
CREATE INDEX IF NOT EXISTS idx_trade_symbol ON trade (symbol);

-- Price alerts
CREATE TABLE IF NOT EXISTS price_alert (
    id              BIGSERIAL PRIMARY KEY,
    user_id         BIGINT       NOT NULL REFERENCES app_user(id) ON DELETE CASCADE,
    symbol          VARCHAR(20)  NOT NULL,
    condition       VARCHAR(10)  NOT NULL,
    target_price    DECIMAL(20, 8) NOT NULL,
    currency        VARCHAR(5)   NOT NULL DEFAULT 'USD',
    status          VARCHAR(10)  NOT NULL DEFAULT 'ACTIVE',
    triggered_at    TIMESTAMP,
    triggered_price DECIMAL(20, 8),
    created_at      TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_alert_user ON price_alert (user_id);

-- Watchlist
CREATE TABLE IF NOT EXISTS watchlist (
    id              BIGSERIAL PRIMARY KEY,
    user_id         BIGINT       NOT NULL REFERENCES app_user(id) ON DELETE CASCADE,
    name            VARCHAR(100) NOT NULL DEFAULT 'My Watchlist',
    created_at      TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS watchlist_item (
    id              BIGSERIAL PRIMARY KEY,
    watchlist_id    BIGINT       NOT NULL REFERENCES watchlist(id) ON DELETE CASCADE,
    symbol          VARCHAR(20)  NOT NULL,
    added_at        TIMESTAMP    NOT NULL DEFAULT NOW(),
    UNIQUE (watchlist_id, symbol)
);

-- Price history (daily OHLCV)
CREATE TABLE IF NOT EXISTS price_history (
    id              BIGSERIAL PRIMARY KEY,
    symbol          VARCHAR(20)  NOT NULL,
    date            DATE         NOT NULL,
    open_price      DECIMAL(20, 8) NOT NULL,
    high_price      DECIMAL(20, 8) NOT NULL,
    low_price       DECIMAL(20, 8) NOT NULL,
    close_price     DECIMAL(20, 8) NOT NULL,
    volume          DECIMAL(30, 2),
    currency        VARCHAR(5)   NOT NULL DEFAULT 'USD',
    UNIQUE (symbol, date)
);

CREATE INDEX IF NOT EXISTS idx_price_history_symbol_date ON price_history (symbol, date DESC);

-- Seed asset master data
INSERT INTO asset (symbol, name, asset_type, coingecko_id, yahoo_symbol, enabled)
VALUES
    ('BTC', 'Bitcoin', 'CRYPTO', 'bitcoin', NULL, true),
    ('ETH', 'Ethereum', 'CRYPTO', 'ethereum', NULL, true),
    ('BNB', 'BNB', 'CRYPTO', 'binancecoin', NULL, true),
    ('SOL', 'Solana', 'CRYPTO', 'solana', NULL, true),
    ('XRP', 'XRP', 'CRYPTO', 'ripple', NULL, true),
    ('ADA', 'Cardano', 'CRYPTO', 'cardano', NULL, true),
    ('DOGE', 'Dogecoin', 'CRYPTO', 'dogecoin', NULL, true),
    ('AAPL', 'Apple Inc.', 'STOCK', NULL, 'AAPL', true),
    ('GOOGL', 'Alphabet Inc.', 'STOCK', NULL, 'GOOGL', true),
    ('MSFT', 'Microsoft Corporation', 'STOCK', NULL, 'MSFT', true),
    ('AMZN', 'Amazon.com Inc.', 'STOCK', NULL, 'AMZN', true),
    ('TSLA', 'Tesla Inc.', 'STOCK', NULL, 'TSLA', true),
    ('NVDA', 'NVIDIA Corporation', 'STOCK', NULL, 'NVDA', true),
    ('META', 'Meta Platforms Inc.', 'STOCK', NULL, 'META', true)
ON CONFLICT (symbol) DO NOTHING;
