package com.stockpulse.domain.alert.dto;

import com.stockpulse.domain.alert.entity.PriceAlert;

import java.math.BigDecimal;
import java.time.Instant;

public record AlertResponse(
        Long id,
        String symbol,
        String condition,
        BigDecimal targetPrice,
        String currency,
        String status,
        Instant triggeredAt,
        BigDecimal triggeredPrice,
        Instant createdAt
) {
    public static AlertResponse from(PriceAlert alert) {
        return new AlertResponse(
                alert.getId(),
                alert.getSymbol(),
                alert.getCondition().name(),
                alert.getTargetPrice(),
                alert.getCurrency(),
                alert.getStatus().name(),
                alert.getTriggeredAt(),
                alert.getTriggeredPrice(),
                alert.getCreatedAt()
        );
    }
}
