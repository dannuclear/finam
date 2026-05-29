package ru.nuclearius.finam.service.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import ru.nuclearius.finam.client.dto.Bar;
import ru.nuclearius.finam.db.Analysis;
import ru.nuclearius.finam.db.AnalysisAsset;
import ru.nuclearius.finam.db.TradeGroup;
import ru.nuclearius.finam.db.TradeGroupReferenceAsset;
import ru.nuclearius.finam.db.TradeGroupTradedAsset;
import ru.nuclearius.finam.rest.dto.AnalysisAssetDTO;
import ru.nuclearius.finam.rest.dto.Indicator;
import ru.nuclearius.finam.rest.dto.TradeGroupDTO;
import ru.nuclearius.finam.rest.dto.TradeGroupReferenceAssetDTO;
import ru.nuclearius.finam.rest.dto.TradeGroupTradedAssetDTO;

@Mapper
public interface EntityMapper {

    @Mapping(target = "id", source = "dto.id")
    @Mapping(target = "tradeGroup", source = "tradeGroup")
    TradeGroupTradedAsset toDomain(TradeGroupTradedAssetDTO dto, TradeGroup tradeGroup);

    @Mapping(target = "id", source = "dto.id")
    @Mapping(target = "tradeGroup", source = "tradeGroup")
    TradeGroupReferenceAsset toDomain(TradeGroupReferenceAssetDTO dto, TradeGroup tradeGroup);

    TradeGroupReferenceAssetDTO toDto(TradeGroupReferenceAsset domain, List<Bar> bars, List<Indicator> sma);

    @Mapping(target = "tradedAssets", ignore = true)
    @Mapping(target = "referenceAssets", ignore = true)
    TradeGroupDTO toDTO(TradeGroup entity);

    @Mapping(target = "id", source = "dto.id")
    @Mapping(target = "analysis", source = "analysis")
    AnalysisAsset toDomain(AnalysisAssetDTO dto, Analysis analysis);
}
