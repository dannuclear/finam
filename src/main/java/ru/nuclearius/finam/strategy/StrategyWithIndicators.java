package ru.nuclearius.finam.strategy;

import java.util.List;

import org.ta4j.core.Strategy;

import ru.nuclearius.finam.rest.dto.ChartIndicatorSeries;

public record StrategyWithIndicators(Strategy strategy, List<ChartIndicatorSeries> indicators) {
    
}