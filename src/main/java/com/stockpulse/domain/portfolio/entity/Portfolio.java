package com.stockpulse.domain.portfolio.entity;

import com.stockpulse.domain.auth.entity.User;
import com.stockpulse.global.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "portfolio", indexes = {
        @Index(name = "idx_portfolio_user", columnList = "user_id")
})
public class Portfolio extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(nullable = false, length = 5)
    private String currency = "USD";

    @OneToMany(mappedBy = "portfolio")
    private List<PortfolioAsset> portfolioAssets = new ArrayList<>();

    protected Portfolio() {
    }

    private Portfolio(User user, String name, String description, String currency) {
        this.user = user;
        this.name = name;
        this.description = description;
        this.currency = currency;
    }

    public static Portfolio create(User user, String name, String description, String currency) {
        return new Portfolio(user, name, description, currency != null ? currency : "USD");
    }

    public void changeName(String name) {
        this.name = name;
    }

    public void changeDescription(String description) {
        this.description = description;
    }

    public boolean isOwnedBy(Long userId) {
        return this.user.getId().equals(userId);
    }

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getCurrency() {
        return currency;
    }

    public List<PortfolioAsset> getPortfolioAssets() {
        return portfolioAssets;
    }
}
