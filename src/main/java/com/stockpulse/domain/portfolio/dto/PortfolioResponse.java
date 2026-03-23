package com.stockpulse.domain.portfolio.dto;

import com.stockpulse.domain.portfolio.entity.Portfolio;

import java.time.Instant;

public record PortfolioResponse(
        Long id,
        String name,
        String description,
        String currency,
        Instant createdAt
) {
    public static PortfolioResponse from(Portfolio portfolio) {
        return new PortfolioResponse(
                portfolio.getId(),
                portfolio.getName(),
                portfolio.getDescription(),
                portfolio.getCurrency(),
                portfolio.getCreatedAt()
        );
    }
}
