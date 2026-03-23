package com.stockpulse.domain.watchlist.service;

import com.stockpulse.domain.auth.entity.User;
import com.stockpulse.domain.auth.repository.UserRepository;
import com.stockpulse.domain.watchlist.dto.WatchlistRequest;
import com.stockpulse.domain.watchlist.entity.Watchlist;
import com.stockpulse.domain.watchlist.entity.WatchlistItem;
import com.stockpulse.domain.watchlist.repository.WatchlistRepository;
import com.stockpulse.global.exception.BusinessException;
import com.stockpulse.global.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class WatchlistService {

    private final WatchlistRepository watchlistRepository;
    private final UserRepository userRepository;

    public WatchlistService(WatchlistRepository watchlistRepository,
                            UserRepository userRepository) {
        this.watchlistRepository = watchlistRepository;
        this.userRepository = userRepository;
    }

    public Watchlist create(WatchlistRequest request, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "User not found"));

        Watchlist watchlist = Watchlist.create(user, request.name());
        return watchlistRepository.save(watchlist);
    }

    @Transactional(readOnly = true)
    public List<Watchlist> findAll(Long userId) {
        return watchlistRepository.findByUserId(userId);
    }

    public Watchlist addItem(Long watchlistId, String symbol, Long userId) {
        Watchlist watchlist = findWatchlistForUser(watchlistId, userId);

        if (watchlist.containsSymbol(symbol)) {
            throw new BusinessException(ErrorCode.WATCHLIST_ITEM_DUPLICATE);
        }

        WatchlistItem item = WatchlistItem.create(watchlist, symbol);
        watchlist.addItem(item);
        return watchlistRepository.save(watchlist);
    }

    public void removeItem(Long watchlistId, String symbol, Long userId) {
        Watchlist watchlist = findWatchlistForUser(watchlistId, userId);
        watchlist.removeItemBySymbol(symbol);
        watchlistRepository.save(watchlist);
    }

    private Watchlist findWatchlistForUser(Long watchlistId, Long userId) {
        Watchlist watchlist = watchlistRepository.findById(watchlistId)
                .orElseThrow(() -> new BusinessException(ErrorCode.WATCHLIST_NOT_FOUND));
        if (!watchlist.isOwnedBy(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        return watchlist;
    }
}
