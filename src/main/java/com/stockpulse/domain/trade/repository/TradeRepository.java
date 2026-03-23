package com.stockpulse.domain.trade.repository;

import com.stockpulse.domain.trade.entity.Trade;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TradeRepository extends JpaRepository<Trade, Long> {

    Page<Trade> findByPortfolioIdOrderByTradedAtDesc(Long portfolioId, Pageable pageable);

    List<Trade> findByPortfolioIdAndSymbol(Long portfolioId, String symbol);
}
