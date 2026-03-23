package com.stockpulse.domain.pricehistory.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "price_history",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"symbol", "date"})
        },
        indexes = {
                @Index(name = "idx_price_history_symbol_date", columnList = "symbol, date DESC")
        }
)
public class PriceHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 20)
    private String symbol;

    @Column(nullable = false)
    private LocalDate date;

    @Column(name = "open_price", nullable = false, precision = 20, scale = 8)
    private BigDecimal openPrice;

    @Column(name = "high_price", nullable = false, precision = 20, scale = 8)
    private BigDecimal highPrice;

    @Column(name = "low_price", nullable = false, precision = 20, scale = 8)
    private BigDecimal lowPrice;

    @Column(name = "close_price", nullable = false, precision = 20, scale = 8)
    private BigDecimal closePrice;

    @Column(precision = 30, scale = 2)
    private BigDecimal volume;

    @Column(nullable = false, length = 5)
    private String currency = "USD";

    protected PriceHistory() {
    }

    private PriceHistory(String symbol, LocalDate date, BigDecimal openPrice,
                         BigDecimal highPrice, BigDecimal lowPrice, BigDecimal closePrice,
                         BigDecimal volume, String currency) {
        this.symbol = symbol;
        this.date = date;
        this.openPrice = openPrice;
        this.highPrice = highPrice;
        this.lowPrice = lowPrice;
        this.closePrice = closePrice;
        this.volume = volume;
        this.currency = currency != null ? currency : "USD";
    }

    public static PriceHistory create(String symbol, LocalDate date, BigDecimal openPrice,
                                      BigDecimal highPrice, BigDecimal lowPrice, BigDecimal closePrice,
                                      BigDecimal volume, String currency) {
        return new PriceHistory(symbol, date, openPrice, highPrice, lowPrice,
                closePrice, volume, currency);
    }

    public Long getId() {
        return id;
    }

    public String getSymbol() {
        return symbol;
    }

    public LocalDate getDate() {
        return date;
    }

    public BigDecimal getOpenPrice() {
        return openPrice;
    }

    public BigDecimal getHighPrice() {
        return highPrice;
    }

    public BigDecimal getLowPrice() {
        return lowPrice;
    }

    public BigDecimal getClosePrice() {
        return closePrice;
    }

    public BigDecimal getVolume() {
        return volume;
    }

    public String getCurrency() {
        return currency;
    }
}
