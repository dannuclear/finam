package ru.nuclearius.finam.strategy.optimizer;

import java.util.List;

import org.springframework.core.task.TaskExecutor;
import org.ta4j.core.BarSeries;
import org.ta4j.core.analysis.cost.CostModel;

import ru.nuclearius.finam.strategy.factory.ChartStrategyFactory;
import ru.nuclearius.finam.strategy.optimizer.parameters.OptimizerStrategyParameter;

public interface StrategyOptimizer {
    OptimizationResult optimize(
            ChartStrategyFactory strategyFactory,
            CostModel costModel,
            BarSeries series,
            List<OptimizerStrategyParameter> optimizerParameter,
            TaskExecutor executor);
}