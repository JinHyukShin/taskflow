package com.stockpulse.domain.asset.repository;

import com.stockpulse.domain.asset.entity.Asset;
import com.stockpulse.domain.asset.entity.AssetType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AssetRepository extends JpaRepository<Asset, Long> {

    Optional<Asset> findBySymbol(String symbol);

    List<Asset> findByAssetTypeAndEnabledTrue(AssetType assetType);

    List<Asset> findByEnabledTrue();

    List<Asset> findBySymbolIn(List<String> symbols);

    boolean existsBySymbol(String symbol);
}
