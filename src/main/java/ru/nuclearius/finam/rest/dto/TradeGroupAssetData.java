package ru.nuclearius.finam.rest.dto;

import java.util.List;
import java.util.Map;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import ru.nuclearius.finam.client.dto.Bar;

@Getter
@Setter
@Builder
public class TradeGroupAssetData {
    private Map<String, List<Indicator>> indicators;
    private List<Bar> bars;
}
