package com.stockpulse.domain.watchlist.entity;

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

import java.time.Instant;

@Entity
@Table(name = "watchlist_item", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"watchlist_id", "symbol"})
})
public class WatchlistItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "watchlist_id", nullable = false)
    private Watchlist watchlist;

    @Column(nullable = false, length = 20)
    private String symbol;

    @Column(name = "added_at", nullable = false, updatable = false)
    private Instant addedAt;

    protected WatchlistItem() {
    }

    private WatchlistItem(Watchlist watchlist, String symbol) {
        this.watchlist = watchlist;
        this.symbol = symbol;
        this.addedAt = Instant.now();
    }

    public static WatchlistItem create(Watchlist watchlist, String symbol) {
        return new WatchlistItem(watchlist, symbol);
    }

    public Long getId() {
        return id;
    }

    public Watchlist getWatchlist() {
        return watchlist;
    }

    public String getSymbol() {
        return symbol;
    }

    public Instant getAddedAt() {
        return addedAt;
    }
}
