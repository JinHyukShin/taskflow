package com.stockpulse.domain.alert.entity;

import com.stockpulse.domain.auth.entity.User;
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
@Table(name = "price_alert", indexes = {
        @Index(name = "idx_alert_user", columnList = "user_id")
})
public class PriceAlert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 20)
    private String symbol;

    @Enumerated(EnumType.STRING)
    @Column(name = "condition", nullable = false, length = 10)
    private AlertCondition condition;

    @Column(name = "target_price", nullable = false, precision = 20, scale = 8)
    private BigDecimal targetPrice;

    @Column(nullable = false, length = 5)
    private String currency = "USD";

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private AlertStatus status = AlertStatus.ACTIVE;

    @Column(name = "triggered_at")
    private Instant triggeredAt;

    @Column(name = "triggered_price", precision = 20, scale = 8)
    private BigDecimal triggeredPrice;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected PriceAlert() {
    }

    private PriceAlert(User user, String symbol, AlertCondition condition,
                       BigDecimal targetPrice, String currency) {
        this.user = user;
        this.symbol = symbol;
        this.condition = condition;
        this.targetPrice = targetPrice;
        this.currency = currency;
        this.status = AlertStatus.ACTIVE;
        this.createdAt = Instant.now();
    }

    public static PriceAlert create(User user, String symbol, AlertCondition condition,
                                    BigDecimal targetPrice, String currency) {
        return new PriceAlert(user, symbol, condition, targetPrice,
                currency != null ? currency : "USD");
    }

    public void trigger(BigDecimal currentPrice) {
        this.status = AlertStatus.TRIGGERED;
        this.triggeredAt = Instant.now();
        this.triggeredPrice = currentPrice;
    }

    public void disable() {
        this.status = AlertStatus.DISABLED;
    }

    public void activate() {
        this.status = AlertStatus.ACTIVE;
        this.triggeredAt = null;
        this.triggeredPrice = null;
    }

    public boolean isTriggered(BigDecimal currentPrice) {
        if (status != AlertStatus.ACTIVE) {
            return false;
        }
        return switch (condition) {
            case ABOVE -> currentPrice.compareTo(targetPrice) >= 0;
            case BELOW -> currentPrice.compareTo(targetPrice) <= 0;
        };
    }

    public void updateTarget(AlertCondition condition, BigDecimal targetPrice) {
        this.condition = condition;
        this.targetPrice = targetPrice;
    }

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public String getSymbol() {
        return symbol;
    }

    public AlertCondition getCondition() {
        return condition;
    }

    public BigDecimal getTargetPrice() {
        return targetPrice;
    }

    public String getCurrency() {
        return currency;
    }

    public AlertStatus getStatus() {
        return status;
    }

    public Instant getTriggeredAt() {
        return triggeredAt;
    }

    public BigDecimal getTriggeredPrice() {
        return triggeredPrice;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
