package ru.nuclearius.finam.service.domain;

import java.util.List;

import lombok.Builder;
import lombok.Getter;
import ru.nuclearius.finam.client.dto.Bar;
import ru.nuclearius.finam.rest.dto.Indicator;

@Getter
@Builder
public class AssetResult {
    private String symbol;
    private List<Bar> bars;
    private List<Indicator> sma;
}
