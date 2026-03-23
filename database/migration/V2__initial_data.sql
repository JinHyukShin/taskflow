-- ==============================================
-- StockPulse V2: Initial Data
-- ==============================================

-- 관리자 계정 (password: admin123! -> BCrypt)
INSERT INTO app_user (email, password_hash, name) VALUES
    ('admin@stockpulse.com', '$2a$12$LJ3m4ys3uz2YHQ8GYs9hPOraJcDMqrdCB6hOMgplTpXMqCBODMxKa', 'Admin');

-- 암호화폐 자산 마스터 데이터 (10개)
INSERT INTO asset (symbol, name, asset_type, coingecko_id, yahoo_symbol, enabled) VALUES
    ('BTC',  'Bitcoin',        'CRYPTO', 'bitcoin',          'BTC-USD',  TRUE),
    ('ETH',  'Ethereum',       'CRYPTO', 'ethereum',         'ETH-USD',  TRUE),
    ('SOL',  'Solana',         'CRYPTO', 'solana',           'SOL-USD',  TRUE),
    ('XRP',  'XRP',            'CRYPTO', 'ripple',           'XRP-USD',  TRUE),
    ('ADA',  'Cardano',        'CRYPTO', 'cardano',          'ADA-USD',  TRUE),
    ('DOGE', 'Dogecoin',       'CRYPTO', 'dogecoin',         'DOGE-USD', TRUE),
    ('DOT',  'Polkadot',       'CRYPTO', 'polkadot',         'DOT-USD',  TRUE),
    ('AVAX', 'Avalanche',      'CRYPTO', 'avalanche-2',      'AVAX-USD', TRUE),
    ('MATIC','Polygon',        'CRYPTO', 'matic-network',    'MATIC-USD',TRUE),
    ('LINK', 'Chainlink',      'CRYPTO', 'chainlink',        'LINK-USD', TRUE);

-- 주식 자산 마스터 데이터 (10개)
INSERT INTO asset (symbol, name, asset_type, coingecko_id, yahoo_symbol, enabled) VALUES
    ('AAPL',  'Apple Inc.',              'STOCK', NULL, 'AAPL',  TRUE),
    ('GOOGL', 'Alphabet Inc.',           'STOCK', NULL, 'GOOGL', TRUE),
    ('MSFT',  'Microsoft Corporation',   'STOCK', NULL, 'MSFT',  TRUE),
    ('TSLA',  'Tesla, Inc.',             'STOCK', NULL, 'TSLA',  TRUE),
    ('AMZN',  'Amazon.com, Inc.',        'STOCK', NULL, 'AMZN',  TRUE),
    ('META',  'Meta Platforms, Inc.',     'STOCK', NULL, 'META',  TRUE),
    ('NVDA',  'NVIDIA Corporation',      'STOCK', NULL, 'NVDA',  TRUE),
    ('JPM',   'JPMorgan Chase & Co.',    'STOCK', NULL, 'JPM',   TRUE),
    ('V',     'Visa Inc.',               'STOCK', NULL, 'V',     TRUE),
    ('JNJ',   'Johnson & Johnson',       'STOCK', NULL, 'JNJ',   TRUE);
