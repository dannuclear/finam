package ru.nuclearius.finam.strategy.optimizer;

import java.math.BigDecimal;
import java.util.Map;

public record OptimizationResult(
		Map<String, String> parameters,
		BigDecimal value) {
}