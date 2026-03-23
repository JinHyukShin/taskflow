package com.stockpulse.domain.trade.service;

import com.stockpulse.domain.portfolio.entity.Portfolio;
import com.stockpulse.domain.portfolio.entity.PortfolioAsset;
import com.stockpulse.domain.portfolio.repository.PortfolioAssetRepository;
import com.stockpulse.domain.portfolio.repository.PortfolioRepository;
import com.stockpulse.domain.trade.dto.TradeRequest;
import com.stockpulse.domain.trade.entity.Trade;
import com.stockpulse.domain.trade.entity.TradeType;
import com.stockpulse.domain.trade.repository.TradeRepository;
import com.stockpulse.global.common.PageResponse;
import com.stockpulse.global.exception.BusinessException;
import com.stockpulse.global.exception.ErrorCode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@Transactional
public class TradeService {

    private final TradeRepository tradeRepository;
    private final PortfolioRepository portfolioRepository;
    private final PortfolioAssetRepository portfolioAssetRepository;

    public TradeService(TradeRepository tradeRepository,
                        PortfolioRepository portfolioRepository,
                        PortfolioAssetRepository portfolioAssetRepository) {
        this.tradeRepository = tradeRepository;
        this.portfolioRepository = portfolioRepository;
        this.portfolioAssetRepository = portfolioAssetRepository;
    }

    public Trade executeTrade(Long portfolioId, TradeRequest request, Long userId) {
        Portfolio portfolio = portfolioRepository.findById(portfolioId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PORTFOLIO_NOT_FOUND));

        if (!portfolio.isOwnedBy(userId)) {
            throw new BusinessException(ErrorCode.PORTFOLIO_ACCESS_DENIED);
        }

        TradeType tradeType = TradeType.valueOf(request.tradeType().toUpperCase());
        BigDecimal totalAmount = request.quantity().multiply(request.pricePerUnit())
                .setScale(2, RoundingMode.HALF_UP);

        // Get or create portfolio asset
        PortfolioAsset portfolioAsset = portfolioAssetRepository
                .findByPortfolioIdAndSymbol(portfolioId, request.symbol())
                .orElseGet(() -> PortfolioAsset.create(portfolio, request.symbol()));

        // Apply trade to portfolio asset
        if (tradeType == TradeType.BUY) {
            portfolioAsset.addBuy(request.quantity(), request.pricePerUnit());
        } else {
            // Check sufficient quantity for sell
            if (portfolioAsset.getTotalQuantity().compareTo(request.quantity()) < 0) {
                throw new BusinessException(ErrorCode.TRADE_INSUFFICIENT_QUANTITY);
            }
            portfolioAsset.addSell(request.quantity());
        }

        portfolioAssetRepository.save(portfolioAsset);

        // Create trade record
        Trade trade = Trade.create(
                portfolio,
                request.symbol(),
                tradeType,
                request.quantity(),
                request.pricePerUnit(),
                totalAmount,
                request.currency() != null ? request.currency() : "USD",
                request.fee(),
                request.note(),
                request.tradedAt()
        );

        return tradeRepository.save(trade);
    }

    @Transactional(readOnly = true)
    public Page<Trade> findTrades(Long portfolioId, Long userId, Pageable pageable) {
        Portfolio portfolio = portfolioRepository.findById(portfolioId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PORTFOLIO_NOT_FOUND));

        if (!portfolio.isOwnedBy(userId)) {
            throw new BusinessException(ErrorCode.PORTFOLIO_ACCESS_DENIED);
        }

        return tradeRepository.findByPortfolioIdOrderByTradedAtDesc(portfolioId, pageable);
    }
}
