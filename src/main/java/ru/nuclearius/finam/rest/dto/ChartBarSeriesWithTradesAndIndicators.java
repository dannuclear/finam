package ru.nuclearius.finam.rest.dto;

import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
public class ChartBarSeriesWithTradesAndIndicators extends ChartBarSeriesWithTrades {
    private List<ChartIndicatorSeries> indicators;
}
