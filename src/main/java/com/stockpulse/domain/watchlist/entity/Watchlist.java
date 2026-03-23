package com.stockpulse.domain.watchlist.entity;

import com.stockpulse.domain.auth.entity.User;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "watchlist")
public class Watchlist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @OneToMany(mappedBy = "watchlist", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<WatchlistItem> items = new ArrayList<>();

    protected Watchlist() {
    }

    private Watchlist(User user, String name) {
        this.user = user;
        this.name = name;
        this.createdAt = Instant.now();
    }

    public static Watchlist create(User user, String name) {
        return new Watchlist(user, name != null ? name : "My Watchlist");
    }

    public void addItem(WatchlistItem item) {
        items.add(item);
    }

    public void removeItemBySymbol(String symbol) {
        items.removeIf(item -> item.getSymbol().equals(symbol));
    }

    public boolean containsSymbol(String symbol) {
        return items.stream().anyMatch(item -> item.getSymbol().equals(symbol));
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

    public Instant getCreatedAt() {
        return createdAt;
    }

    public List<WatchlistItem> getItems() {
        return items;
    }
}
