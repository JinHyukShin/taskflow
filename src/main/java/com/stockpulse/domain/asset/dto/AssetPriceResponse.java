package com.stockpulse.domain.asset.dto;

import com.stockpulse.infra.external.dto.PriceData;

import java.math.BigDecimal;
import java.time.Instant;

public record AssetPriceResponse(
        String symbol,
        String name,
        String assetType,
        BigDecimal price,
        String currency,
        BigDecimal change24h,
        BigDecimal changePercent24h,
        BigDecimal volume24h,
        BigDecimal marketCap,
        Instant timestamp
) {
    public static AssetPriceResponse from(PriceData priceData) {
        return new AssetPriceResponse(
                priceData.symbol(),
                priceData.name(),
                null,
                BigDecimal.valueOf(priceData.price()),
                priceData.currency(),
                BigDecimal.valueOf(priceData.change24h()),
                BigDecimal.valueOf(priceData.changePercent24h()),
                BigDecimal.valueOf(priceData.volume24h()),
                BigDecimal.valueOf(priceData.marketCap()),
                priceData.timestamp()
        );
    }
}
