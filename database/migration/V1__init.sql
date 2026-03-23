-- ==============================================
-- StockPulse V1: Initial Schema
-- ==============================================

-- 사용자
CREATE TABLE app_user (
    id              BIGSERIAL PRIMARY KEY,
    email           VARCHAR(255) NOT NULL UNIQUE,
    password_hash   VARCHAR(255) NOT NULL,
    name            VARCHAR(100) NOT NULL,
    created_at      TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP    NOT NULL DEFAULT NOW()
);

-- 리프레시 토큰
CREATE TABLE refresh_token (
    id              BIGSERIAL PRIMARY KEY,
    user_id         BIGINT       NOT NULL REFERENCES app_user(id) ON DELETE CASCADE,
    token           VARCHAR(512) NOT NULL UNIQUE,
    expires_at      TIMESTAMP    NOT NULL,
    created_at      TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_refresh_token_user_id ON refresh_token (user_id);
CREATE INDEX idx_refresh_token_expires_at ON refresh_token (expires_at);

-- 자산 마스터
CREATE TABLE asset (
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

CREATE INDEX idx_asset_type ON asset (asset_type);

-- 포트폴리오
CREATE TABLE portfolio (
    id              BIGSERIAL PRIMARY KEY,
    user_id         BIGINT       NOT NULL REFERENCES app_user(id) ON DELETE CASCADE,
    name            VARCHAR(100) NOT NULL,
    description     VARCHAR(500),
    currency        VARCHAR(5)   NOT NULL DEFAULT 'USD',
    created_at      TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_portfolio_user ON portfolio (user_id);

-- 포트폴리오 보유 자산
CREATE TABLE portfolio_asset (
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

-- 거래 내역
CREATE TABLE trade (
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

CREATE INDEX idx_trade_portfolio ON trade (portfolio_id, traded_at DESC);
CREATE INDEX idx_trade_symbol ON trade (symbol);

-- 가격 알림
CREATE TABLE price_alert (
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

CREATE INDEX idx_alert_user ON price_alert (user_id);
CREATE INDEX idx_alert_active ON price_alert (status, symbol) WHERE status = 'ACTIVE';

-- 관심종목
CREATE TABLE watchlist (
    id              BIGSERIAL PRIMARY KEY,
    user_id         BIGINT       NOT NULL REFERENCES app_user(id) ON DELETE CASCADE,
    name            VARCHAR(100) NOT NULL DEFAULT 'My Watchlist',
    created_at      TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_watchlist_user ON watchlist (user_id);

CREATE TABLE watchlist_item (
    id              BIGSERIAL PRIMARY KEY,
    watchlist_id    BIGINT       NOT NULL REFERENCES watchlist(id) ON DELETE CASCADE,
    symbol          VARCHAR(20)  NOT NULL,
    added_at        TIMESTAMP    NOT NULL DEFAULT NOW(),
    UNIQUE (watchlist_id, symbol)
);

-- 가격 히스토리 (일별 OHLCV)
CREATE TABLE price_history (
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

CREATE INDEX idx_price_history_symbol_date ON price_history (symbol, date DESC);
