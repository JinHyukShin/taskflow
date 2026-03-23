package com.stockpulse.infra.external.dto;

import java.math.BigDecimal;
import java.util.List;

public record YahooFinanceResponse(
        Chart chart
) {
    public record Chart(
            List<Result> result
    ) {
    }

    public record Result(
            List<Long> timestamp,
            Indicators indicators
    ) {
    }

    public record Indicators(
            List<Quote> quote
    ) {
    }

    public record Quote(
            List<BigDecimal> open,
            List<BigDecimal> high,
            List<BigDecimal> low,
            List<BigDecimal> close,
            List<BigDecimal> volume
    ) {
    }
}
