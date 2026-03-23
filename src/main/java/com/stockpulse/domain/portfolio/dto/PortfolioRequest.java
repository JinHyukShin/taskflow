package com.stockpulse.domain.portfolio.dto;

import jakarta.validation.constraints.NotBlank;

public record PortfolioRequest(
        @NotBlank String name,
        String description,
        String currency
) {
}
