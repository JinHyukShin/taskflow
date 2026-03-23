package com.stockpulse.infra.external.dto;

import java.math.BigDecimal;
import java.util.Map;

public record CoinGeckoResponse(
        Map<String, CoinPrice> prices
) {
    public record CoinPrice(
            BigDecimal usd,
            BigDecimal usd_24h_change,
            BigDecimal usd_24h_vol,
            BigDecimal usd_market_cap
    ) {
    }
}
