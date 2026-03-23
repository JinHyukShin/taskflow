package com.stockpulse.domain.alert.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record AlertRequest(
        @NotBlank String symbol,
        @NotBlank String condition,
        @NotNull @Positive BigDecimal targetPrice,
        String currency
) {
}
