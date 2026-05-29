package ru.nuclearius.finam.repository;

import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ru.nuclearius.finam.db.TradeGroupTradedAsset;

@Repository
public interface TradeGroupTradedAssetRepository extends JpaRepository<TradeGroupTradedAsset, Integer> {

    // Все торгуемые инструменты для группы
    @EntityGraph(attributePaths = { "asset" })
    List<TradeGroupTradedAsset> findByTradeGroupId(Integer tradeGroupId);

    // Найти конкретную связь группы и инструмента
    @EntityGraph(attributePaths = { "asset" })
    TradeGroupTradedAsset findByTradeGroupIdAndAssetId(Integer tradeGroupId, String assetId);

    // Удалить все торгуемые инструменты группы
    void deleteByTradeGroupId(Integer tradeGroupId);

    // Проверка, есть ли связь между группой и инструментом
    boolean existsByTradeGroupIdAndAssetId(Integer tradeGroupId, String assetId);
}