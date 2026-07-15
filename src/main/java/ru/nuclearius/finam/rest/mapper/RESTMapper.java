package ru.nuclearius.finam.rest.mapper;

import java.util.List;
import java.util.Map;

import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import ru.nuclearius.finam.client.dto.Bar;
import ru.nuclearius.finam.client.dto.TradeHistory;
import ru.nuclearius.finam.db.AnalysisAsset;
import ru.nuclearius.finam.db.TradeGroupReferenceAsset;
import ru.nuclearius.finam.rest.dto.Series;

@Mapper(builder = @Builder(disableBuilder = true))
public interface RESTMapper {
    @Mapping(target = "name", source = "domain.asset.name")
    Series toDto(TradeGroupReferenceAsset domain, List<Bar> bars, Map<String, Object> extraParams);

    @Mapping(target = "name", source = "domain.asset.name")
    @Mapping(target = "extraParams", ignore = true)
    Series toDto(TradeGroupReferenceAsset domain, List<Bar> bars);

    @Mapping(target = "id", source = "domain.asset.symbol")
    @Mapping(target = "name", source = "domain.asset.name")
    @Mapping(target = "panelNum", source = "domain.panelIndex")
    @Mapping(target = "extraParams", ignore = true)
    Series toDto(AnalysisAsset domain, List<Bar> bars);

    @Mapping(target = "id", source = "domain.asset.symbol")
    @Mapping(target = "name", source = "domain.asset.name")
    @Mapping(target = "panelNum", source = "domain.panelIndex")
    @Mapping(target = "extraParams", ignore = true)
    Series toDto(AnalysisAsset domain, List<Bar> bars, List<TradeHistory.Trade> trades);

    @Mapping(target = "name", source = "domain.asset.name")
    @Mapping(target = "panelNum", source = "domain.panelIndex")
    Series toDto(AnalysisAsset domain, List<Bar> bars, Map<String, Object> extraParams);
}
