package com.stockpulse.domain.trade.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.Instant;

public record TradeRequest(
        @NotBlank String symbol,
        @NotBlank String tradeType,
        @NotNull @Positive BigDecimal quantity,
        @NotNull @Positive BigDecimal pricePerUnit,
        String currency,
        BigDecimal fee,
        String note,
        @NotNull Instant tradedAt
) {
}
