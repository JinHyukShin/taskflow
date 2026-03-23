package com.stockpulse.domain.pricehistory.dto;

import com.stockpulse.domain.pricehistory.entity.PriceHistory;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CandlestickData(
        LocalDate date,
        BigDecimal open,
        BigDecimal high,
        BigDecimal low,
        BigDecimal close,
        BigDecimal volume
) {
    public static CandlestickData from(PriceHistory history) {
        return new CandlestickData(
                history.getDate(),
                history.getOpenPrice(),
                history.getHighPrice(),
                history.getLowPrice(),
                history.getClosePrice(),
                history.getVolume()
        );
    }
}
