package com.stockpulse.domain.trade.dto;

import com.stockpulse.domain.trade.entity.Trade;

import java.math.BigDecimal;
import java.time.Instant;

public record TradeResponse(
        Long tradeId,
        String symbol,
        String tradeType,
        BigDecimal quantity,
        BigDecimal pricePerUnit,
        BigDecimal totalAmount,
        String currency,
        BigDecimal fee,
        String note,
        Instant tradedAt,
        Instant createdAt
) {
    public static TradeResponse from(Trade trade) {
        return new TradeResponse(
                trade.getId(),
                trade.getSymbol(),
                trade.getTradeType().name(),
                trade.getQuantity(),
                trade.getPricePerUnit(),
                trade.getTotalAmount(),
                trade.getCurrency(),
                trade.getFee(),
                trade.getNote(),
                trade.getTradedAt(),
                trade.getCreatedAt()
        );
    }
}
