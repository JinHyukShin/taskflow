package com.stockpulse.domain.portfolio.entity;

import com.stockpulse.global.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Entity
@Table(name = "portfolio_asset", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"portfolio_id", "symbol"})
})
public class PortfolioAsset extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "portfolio_id", nullable = false)
    private Portfolio portfolio;

    @Column(nullable = false, length = 20)
    private String symbol;

    @Column(name = "total_quantity", nullable = false, precision = 20, scale = 8)
    private BigDecimal totalQuantity = BigDecimal.ZERO;

    @Column(name = "avg_buy_price", nullable = false, precision = 20, scale = 8)
    private BigDecimal avgBuyPrice = BigDecimal.ZERO;

    @Column(name = "total_invested", nullable = false, precision = 20, scale = 2)
    private BigDecimal totalInvested = BigDecimal.ZERO;

    protected PortfolioAsset() {
    }

    private PortfolioAsset(Portfolio portfolio, String symbol) {
        this.portfolio = portfolio;
        this.symbol = symbol;
    }

    public static PortfolioAsset create(Portfolio portfolio, String symbol) {
        return new PortfolioAsset(portfolio, symbol);
    }

    public void addBuy(BigDecimal quantity, BigDecimal pricePerUnit) {
        BigDecimal buyAmount = quantity.multiply(pricePerUnit);
        this.totalInvested = this.totalInvested.add(buyAmount);
        this.totalQuantity = this.totalQuantity.add(quantity);
        if (this.totalQuantity.compareTo(BigDecimal.ZERO) > 0) {
            this.avgBuyPrice = this.totalInvested
                    .divide(this.totalQuantity, 8, RoundingMode.HALF_UP);
        }
    }

    /**
     * Apply sell trade.
     * Average buy price stays the same (unchanged).
     * new_total_invested = avg_buy_price * new_total_quantity
     */
    public void addSell(BigDecimal quantity) {
        this.totalQuantity = this.totalQuantity.subtract(quantity);
        if (this.totalQuantity.compareTo(BigDecimal.ZERO) <= 0) {
            this.totalQuantity = BigDecimal.ZERO;
            this.totalInvested = BigDecimal.ZERO;
            this.avgBuyPrice = BigDecimal.ZERO;
        } else {
            this.totalInvested = this.avgBuyPrice.multiply(this.totalQuantity)
                    .setScale(2, RoundingMode.HALF_UP);
        }
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

    public BigDecimal getTotalQuantity() {
        return totalQuantity;
    }

    public BigDecimal getAvgBuyPrice() {
        return avgBuyPrice;
    }

    public BigDecimal getTotalInvested() {
        return totalInvested;
    }
}
