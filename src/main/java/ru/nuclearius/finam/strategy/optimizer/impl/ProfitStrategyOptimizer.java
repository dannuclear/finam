package ru.nuclearius.finam.strategy.optimizer.impl;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

import org.springframework.core.task.TaskExecutor;
import org.ta4j.core.BarSeries;
import org.ta4j.core.TradingRecord;
import org.ta4j.core.analysis.cost.CostModel;
import org.ta4j.core.analysis.cost.ZeroCostModel;
import org.ta4j.core.backtest.BarSeriesManager;
import org.ta4j.core.criteria.ReturnRepresentation;
import org.ta4j.core.criteria.pnl.NetReturnCriterion;
import org.ta4j.core.num.Num;

import lombok.extern.slf4j.Slf4j;
import ru.nuclearius.finam.strategy.StrategyWithIndicators;
import ru.nuclearius.finam.strategy.factory.ChartStrategyFactory;
import ru.nuclearius.finam.strategy.optimizer.OptimizationResult;
import ru.nuclearius.finam.strategy.optimizer.StrategyOptimizer;
import ru.nuclearius.finam.strategy.optimizer.parameters.OptimizerStrategyParameter;

@Slf4j
public class ProfitStrategyOptimizer implements StrategyOptimizer {
    @Override
    public OptimizationResult optimize(
            ChartStrategyFactory strategyFactory,
            CostModel costModel,
            BarSeries series,
            List<OptimizerStrategyParameter> params,
            TaskExecutor executor) {

        List<CompletableFuture<OptimizationResult>> features = cartesianProduct(params)
                .map(p -> CompletableFuture.supplyAsync(() -> {
                    StrategyWithIndicators si = strategyFactory.build(series, p);
                    BarSeriesManager manager = new BarSeriesManager(
                            series,
                            costModel,
                            new ZeroCostModel());

                    TradingRecord tradingRecord = manager.run(si.strategy());

                    Num result = new NetReturnCriterion(ReturnRepresentation.PERCENTAGE)
                            .calculate(series, tradingRecord);
                    return new OptimizationResult(p, result.bigDecimalValue());
                }, executor)).toList();

        OptimizationResult best = features.stream()
                .map(CompletableFuture::join)
                .max(Comparator.comparingDouble(or -> or.value().doubleValue()))
                .orElseThrow();
        return best;
    }

    public static Stream<Map<String, String>> cartesianProduct(
            List<OptimizerStrategyParameter> parameters) {

        Stream<Map<String, String>> result = Stream.of(new HashMap<>());

        for (OptimizerStrategyParameter parameter : parameters) {
            result = result.flatMap(current -> parameter.getValues().stream()
                    .map(value -> {
                        Map<String, String> next = new HashMap<>(current);
                        next.put(parameter.getId(), String.valueOf(value));
                        return next;
                    }));
        }

        return result;
    }
}
