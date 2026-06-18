package ru.nuclearius.finam.repository;

import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ru.nuclearius.finam.db.StrategyAsset;

@Repository
public interface StrategyAssetRepository extends JpaRepository<StrategyAsset, Integer> {

    @EntityGraph(attributePaths = { "asset" })
    List<StrategyAsset> findByStrategyId(Integer tradeGroupId);

    void deleteByStrategyId(Integer strategyId);

    boolean existsByStrategyIdAndAssetId(Integer strategyId, String assetId);
}