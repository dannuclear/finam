package ru.nuclearius.finam.service.mapper;

import java.math.BigDecimal;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.ta4j.core.num.Num;

import ru.nuclearius.finam.client.dto.Bar;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface Ta4jMapper {
    @Mapping(target = "open", source = "openPrice")
    @Mapping(target = "high", source = "highPrice")
    @Mapping(target = "low", source = "lowPrice")
    @Mapping(target = "close", source = "closePrice")
    @Mapping(target = "timestamp", source = "endTime")
    Bar map(org.ta4j.core.Bar bar);

    default BigDecimal map(Num value) {
        if (value == null)
            return null;
        return value.bigDecimalValue();
    }
}