package com.stockpulse.domain.portfolio.repository;

import com.stockpulse.domain.portfolio.entity.PortfolioAsset;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PortfolioAssetRepository extends JpaRepository<PortfolioAsset, Long> {

    List<PortfolioAsset> findByPortfolioId(Long portfolioId);

    Optional<PortfolioAsset> findByPortfolioIdAndSymbol(Long portfolioId, String symbol);
}
