package com.stockpulse.domain.asset.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "asset", indexes = {
        @Index(name = "idx_asset_type", columnList = "asset_type")
})
public class Asset {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String symbol;

    @Column(nullable = false, length = 200)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "asset_type", nullable = false, length = 10)
    private AssetType assetType;

    @Column(name = "coingecko_id", length = 100)
    private String coingeckoId;

    @Column(name = "yahoo_symbol", length = 20)
    private String yahooSymbol;

    @Column(name = "logo_url", length = 500)
    private String logoUrl;

    @Column(nullable = false)
    private boolean enabled = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected Asset() {
    }

    private Asset(String symbol, String name, AssetType assetType,
                  String coingeckoId, String yahooSymbol, String logoUrl) {
        this.symbol = symbol;
        this.name = name;
        this.assetType = assetType;
        this.coingeckoId = coingeckoId;
        this.yahooSymbol = yahooSymbol;
        this.logoUrl = logoUrl;
        this.enabled = true;
        this.createdAt = Instant.now();
    }

    public static Asset create(String symbol, String name, AssetType assetType,
                               String coingeckoId, String yahooSymbol, String logoUrl) {
        return new Asset(symbol, name, assetType, coingeckoId, yahooSymbol, logoUrl);
    }

    public void disable() {
        this.enabled = false;
    }

    public void enable() {
        this.enabled = true;
    }

    public Long getId() {
        return id;
    }

    public String getSymbol() {
        return symbol;
    }

    public String getName() {
        return name;
    }

    public AssetType getAssetType() {
        return assetType;
    }

    public String getCoingeckoId() {
        return coingeckoId;
    }

    public String getYahooSymbol() {
        return yahooSymbol;
    }

    public String getLogoUrl() {
        return logoUrl;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
