package com.stockpulse.domain.portfolio.service;

import com.stockpulse.domain.auth.entity.User;
import com.stockpulse.domain.auth.repository.UserRepository;
import com.stockpulse.domain.portfolio.dto.PortfolioRequest;
import com.stockpulse.domain.portfolio.dto.PortfolioSummaryResponse;
import com.stockpulse.domain.portfolio.entity.Portfolio;
import com.stockpulse.domain.portfolio.entity.PortfolioAsset;
import com.stockpulse.domain.portfolio.repository.PortfolioAssetRepository;
import com.stockpulse.domain.portfolio.repository.PortfolioRepository;
import com.stockpulse.global.exception.BusinessException;
import com.stockpulse.global.exception.ErrorCode;
import com.stockpulse.infra.external.dto.PriceData;
import com.stockpulse.infra.redis.PriceCacheService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class PortfolioService {

    private final PortfolioRepository portfolioRepository;
    private final PortfolioAssetRepository portfolioAssetRepository;
    private final UserRepository userRepository;
    private final PriceCacheService cacheService;

    public PortfolioService(PortfolioRepository portfolioRepository,
                            PortfolioAssetRepository portfolioAssetRepository,
                            UserRepository userRepository,
                            PriceCacheService cacheService) {
        this.portfolioRepository = portfolioRepository;
        this.portfolioAssetRepository = portfolioAssetRepository;
        this.userRepository = userRepository;
        this.cacheService = cacheService;
    }

    public Portfolio create(PortfolioRequest request, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "User not found"));

        Portfolio portfolio = Portfolio.create(user, request.name(), request.description(), request.currency());
        return portfolioRepository.save(portfolio);
    }

    @Transactional(readOnly = true)
    public List<Portfolio> findAll(Long userId) {
        return portfolioRepository.findByUserId(userId);
    }

    @Transactional(readOnly = true)
    public Portfolio findById(Long portfolioId, Long userId) {
        Portfolio portfolio = portfolioRepository.findById(portfolioId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PORTFOLIO_NOT_FOUND));
        if (!portfolio.isOwnedBy(userId)) {
            throw new BusinessException(ErrorCode.PORTFOLIO_ACCESS_DENIED);
        }
        return portfolio;
    }

    @Transactional(readOnly = true)
    public PortfolioSummaryResponse getSummary(Long portfolioId, Long userId) {
        Portfolio portfolio = findById(portfolioId, userId);
        List<PortfolioAsset> assets = portfolioAssetRepository.findByPortfolioId(portfolioId);

        if (assets.isEmpty()) {
            return new PortfolioSummaryResponse(
                    portfolioId, portfolio.getName(),
                    BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                    List.of()
            );
        }

        // Fetch current prices from Redis
        List<String> symbols = assets.stream().map(PortfolioAsset::getSymbol).toList();
        Map<String, PriceData> currentPrices = cacheService.getPrices(symbols);

        BigDecimal totalInvested = BigDecimal.ZERO;
        BigDecimal totalCurrentValue = BigDecimal.ZERO;
        List<PortfolioSummaryResponse.AssetHolding> holdings = new ArrayList<>();

        for (PortfolioAsset pa : assets) {
            if (pa.getTotalQuantity().compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }

            PriceData priceData = currentPrices.get(pa.getSymbol());
            BigDecimal currentPrice = priceData != null
                    ? BigDecimal.valueOf(priceData.price())
                    : pa.getAvgBuyPrice(); // fallback to avg buy price if no current price

            BigDecimal currentValue = pa.getTotalQuantity().multiply(currentPrice)
                    .setScale(2, RoundingMode.HALF_UP);
            BigDecimal invested = pa.getTotalInvested();
            BigDecimal pnl = currentValue.subtract(invested);
            BigDecimal pnlPercent = invested.compareTo(BigDecimal.ZERO) > 0
                    ? pnl.divide(invested, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;

            totalInvested = totalInvested.add(invested);
            totalCurrentValue = totalCurrentValue.add(currentValue);

            holdings.add(new PortfolioSummaryResponse.AssetHolding(
                    pa.getSymbol(),
                    priceData != null ? priceData.name() : pa.getSymbol(),
                    pa.getTotalQuantity(),
                    pa.getAvgBuyPrice(),
                    currentPrice,
                    currentValue,
                    pnl,
                    pnlPercent,
                    BigDecimal.ZERO // allocation calculated below
            ));
        }

        // Calculate allocation percentages
        BigDecimal finalTotalCurrentValue = totalCurrentValue;
        List<PortfolioSummaryResponse.AssetHolding> finalHoldings = holdings.stream()
                .map(h -> {
                    BigDecimal allocation = finalTotalCurrentValue.compareTo(BigDecimal.ZERO) > 0
                            ? h.currentValue().divide(finalTotalCurrentValue, 4, RoundingMode.HALF_UP)
                            .multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP)
                            : BigDecimal.ZERO;
                    return new PortfolioSummaryResponse.AssetHolding(
                            h.symbol(), h.name(), h.quantity(), h.avgBuyPrice(),
                            h.currentPrice(), h.currentValue(), h.pnl(), h.pnlPercent(),
                            allocation
                    );
                })
                .toList();

        BigDecimal totalPnl = totalCurrentValue.subtract(totalInvested);
        BigDecimal totalPnlPercent = totalInvested.compareTo(BigDecimal.ZERO) > 0
                ? totalPnl.divide(totalInvested, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        return new PortfolioSummaryResponse(
                portfolioId,
                portfolio.getName(),
                totalInvested,
                totalCurrentValue,
                totalPnl,
                totalPnlPercent,
                finalHoldings
        );
    }

    public void delete(Long portfolioId, Long userId) {
        Portfolio portfolio = findById(portfolioId, userId);
        portfolioRepository.delete(portfolio);
    }
}
