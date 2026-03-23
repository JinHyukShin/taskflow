package com.stockpulse.infra.external.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record PriceData(
        String symbol,
        String name,
        double price,
        String currency,
        double change24h,
        double changePercent24h,
        double volume24h,
        double marketCap,
        Instant timestamp,
        boolean stale
) {
    public PriceData(String symbol, String name, double price, String currency,
                     double change24h, double changePercent24h, double volume24h,
                     double marketCap, Instant timestamp) {
        this(symbol, name, price, currency, change24h, changePercent24h,
                volume24h, marketCap, timestamp, false);
    }

    public PriceData withStale(boolean stale) {
        return new PriceData(symbol, name, price, currency, change24h,
                changePercent24h, volume24h, marketCap, timestamp, stale);
    }
}
