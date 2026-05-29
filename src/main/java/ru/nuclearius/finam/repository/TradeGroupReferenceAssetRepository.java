package ru.nuclearius.finam.repository;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import ru.nuclearius.finam.db.TradeGroupReferenceAsset;

@Repository
public interface TradeGroupReferenceAssetRepository extends JpaRepository<TradeGroupReferenceAsset, Integer> {

    // Все опорные инструменты для группы
    @EntityGraph(attributePaths = { "asset" })
    List<TradeGroupReferenceAsset> findByTradeGroupId(Integer tradeGroupId);

    // Найти конкретную связь группы и инструмента
    @EntityGraph(attributePaths = { "asset" })
    TradeGroupReferenceAsset findByTradeGroupIdAndAssetId(Integer tradeGroupId, String assetId);

    // Удалить все опорные инструменты группы
    void deleteByTradeGroupId(Integer tradeGroupId);

    // Проверка, есть ли связь между группой и инструментом
    boolean existsByTradeGroupIdAndAssetId(Integer tradeGroupId, String assetId);

    @Modifying
    @Transactional
    @Query("update TradeGroupReferenceAsset t set t.priceOffset = :priceOffset where t.id = :id")
    int updatePriceOffsetById(@Param("id") Integer id, @Param("priceOffset") BigDecimal priceOffset);
}