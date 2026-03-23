package com.stockpulse.domain.trade.entity;

import com.stockpulse.domain.portfolio.entity.Portfolio;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "trade", indexes = {
        @Index(name = "idx_trade_portfolio", columnList = "portfolio_id, traded_at DESC"),
        @Index(name = "idx_trade_symbol", columnList = "symbol")
})
public class Trade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "portfolio_id", nullable = false)
    private Portfolio portfolio;

    @Column(nullable = false, length = 20)
    private String symbol;

    @Enumerated(EnumType.STRING)
    @Column(name = "trade_type", nullable = false, length = 4)
    private TradeType tradeType;

    @Column(nullable = false, precision = 20, scale = 8)
    private BigDecimal quantity;

    @Column(name = "price_per_unit", nullable = false, precision = 20, scale = 8)
    private BigDecimal pricePerUnit;

    @Column(name = "total_amount", nullable = false, precision = 20, scale = 2)
    private BigDecimal totalAmount;

    @Column(nullable = false, length = 5)
    private String currency = "USD";

    @Column(precision = 20, scale = 2)
    private BigDecimal fee = BigDecimal.ZERO;

    @Column(length = 500)
    private String note;

    @Column(name = "traded_at", nullable = false)
    private Instant tradedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected Trade() {
    }

    private Trade(Portfolio portfolio, String symbol, TradeType tradeType,
                  BigDecimal quantity, BigDecimal pricePerUnit, BigDecimal totalAmount,
                  String currency, BigDecimal fee, String note, Instant tradedAt) {
        this.portfolio = portfolio;
        this.symbol = symbol;
        this.tradeType = tradeType;
        this.quantity = quantity;
        this.pricePerUnit = pricePerUnit;
        this.totalAmount = totalAmount;
        this.currency = currency;
        this.fee = fee != null ? fee : BigDecimal.ZERO;
        this.note = note;
        this.tradedAt = tradedAt;
        this.createdAt = Instant.now();
    }

    public static Trade create(Portfolio portfolio, String symbol, TradeType tradeType,
                               BigDecimal quantity, BigDecimal pricePerUnit, BigDecimal totalAmount,
                               String currency, BigDecimal fee, String note, Instant tradedAt) {
        return new Trade(portfolio, symbol, tradeType, quantity, pricePerUnit,
                totalAmount, currency, fee, note, tradedAt);
    }

    public Long getId() {
        return id;
    }

    public Portfolio getPortfolio() {
        return portfolio;
    }

    public String getSymbol() {
        return symbol;
    }

    public TradeType getTradeType() {
        return tradeType;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public BigDecimal getPricePerUnit() {
        return pricePerUnit;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public String getCurrency() {
        return currency;
    }

    public BigDecimal getFee() {
        return fee;
    }

    public String getNote() {
        return note;
    }

    public Instant getTradedAt() {
        return tradedAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
