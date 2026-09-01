package ru.nuclearius.finam.strategy;

import java.util.List;

import org.ta4j.core.AnalysisCriterion;
import org.ta4j.core.Strategy;

import lombok.AllArgsConstructor;
import lombok.Getter;
import ru.nuclearius.finam.rest.dto.ChartIndicatorSeries;

public interface ChartStrategy {
    Strategy getStrategy();

    List<ChartIndicatorSeries> indicators();

    @Getter
    @AllArgsConstructor
    public static class NamedAnalysisCriterion {
        private final String name;
        private final AnalysisCriterion analysisCriterion;
    }
}
