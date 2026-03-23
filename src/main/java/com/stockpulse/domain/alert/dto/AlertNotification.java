package com.stockpulse.domain.alert.dto;

import com.stockpulse.domain.alert.entity.PriceAlert;

import java.math.BigDecimal;
import java.time.Instant;

public record AlertNotification(
        Long alertId,
        String symbol,
        String condition,
        BigDecimal targetPrice,
        BigDecimal triggeredPrice,
        Instant triggeredAt
) {
    public static AlertNotification from(PriceAlert alert) {
        return new AlertNotification(
                alert.getId(),
                alert.getSymbol(),
                alert.getCondition().name(),
                alert.getTargetPrice(),
                alert.getTriggeredPrice(),
                alert.getTriggeredAt()
        );
    }
}
