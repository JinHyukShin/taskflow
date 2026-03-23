package com.stockpulse.infra.external.dto;

public record CandlestickData(
        long time,
        double open,
        double high,
        double low,
        double close,
        double volume
) {
}
