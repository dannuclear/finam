package ru.nuclearius.finam.rest.controller;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.core.task.TaskExecutor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.ta4j.core.TradingRecord;
import org.ta4j.core.analysis.cost.CostModel;
import org.ta4j.core.analysis.cost.LinearTransactionCostModel;
import org.ta4j.core.analysis.cost.ZeroCostModel;
import org.ta4j.core.backtest.BarSeriesManager;

import grpc.tradeapi.v1.marketdata.TimeFrame;
import lombok.RequiredArgsConstructor;
import ru.nuclearius.finam.db.Strategy;
import ru.nuclearius.finam.db.StrategyAsset;
import ru.nuclearius.finam.repository.AssetRepository;
import ru.nuclearius.finam.rest.dto.BacktestResult;
import ru.nuclearius.finam.rest.dto.ChartBacktestSeries;
import ru.nuclearius.finam.rest.dto.ChartBarSeries;
import ru.nuclearius.finam.rest.dto.ChartSeriesMarker;
import ru.nuclearius.finam.rest.dto.StrategyDto;
import ru.nuclearius.finam.service.BarService;
import ru.nuclearius.finam.service.StrategyService;
import ru.nuclearius.finam.strategy.StrategyWithIndicators;
import ru.nuclearius.finam.strategy.factory.ChartStrategyFactory;
import ru.nuclearius.finam.strategy.factory.StrategyParameter;
import ru.nuclearius.finam.strategy.factory.impl.MovingAverageStrategyFactory;
import ru.nuclearius.finam.strategy.optimizer.OptimizationResult;
import ru.nuclearius.finam.strategy.optimizer.StrategyOptimizer;
import ru.nuclearius.finam.strategy.optimizer.impl.ProfitStrategyOptimizer;
import ru.nuclearius.finam.strategy.optimizer.parameters.OptimizerStrategyParameter;
import ru.nuclearius.finam.utils.BacktestUtils;
import ru.nuclearius.finam.utils.BarUtils;
import ru.nuclearius.finam.utils.StatisticsUtils;

@RestController
@RequestMapping("api/v1/strategies")
@RequiredArgsConstructor
public class StrategyController {
	private final BarService barService;
	private final StrategyService strategyService;
	private final AssetRepository assetRepository;
	private final TaskExecutor barsExecutor;

	@GetMapping
	public Page<Strategy> all(
			@RequestParam(required = false) String q,
			@ParameterObject Pageable pageable) {
		return strategyService.findAll(q, pageable);
	}

	@GetMapping("{id:\\d+}")
	public Strategy byId(@PathVariable Integer id) {
		return strategyService.getById(id);
	}

	@GetMapping("{id:\\d+}/assets")
	public List<StrategyAsset> assets(@PathVariable Integer id) {
		return strategyService.assets(id);
	}

	@PostMapping
	public Strategy create(@RequestBody StrategyDto dto) {
		return strategyService.create(dto.name(), dto.assets());
	}

	@PutMapping("{id:\\d+}")
	public Strategy update(@PathVariable Integer id, @RequestBody StrategyDto dto) {
		return strategyService.update(id, dto.name(), dto.assets());
	}

	@DeleteMapping("{id:\\d+}")
	public void delete(@PathVariable Integer id) {
		strategyService.delete(id);
	}

	@GetMapping("{id:\\d+}/parameters")
	public List<StrategyParameter> strategyParameters(@PathVariable Integer id) {
		return new MovingAverageStrategyFactory().getParameters();
	}

	@PostMapping("{id:\\d+}/backtest")
	public BacktestResult backtest(
			@PathVariable Integer id,
			@RequestParam List<String> assets,
			@RequestParam TimeFrame timeFrame,
			@RequestParam Instant startTime,
			@RequestParam Instant endTime,
			@RequestParam Double transactionCost,
			@RequestParam Integer distributionMa,
			@RequestBody Map<String, String> strategyParams) {

		CostModel transactionCostModel = new LinearTransactionCostModel(transactionCost / 100.0);
		ChartStrategyFactory chartStrategyFactory = new MovingAverageStrategyFactory();

		List<ChartBacktestSeries> chartSeries = assetRepository.findAllBySymbolIn(assets).stream().map(asset -> {
			return barService.ta4jSeriesAsync(asset.getSymbol(), timeFrame, startTime, endTime, false)
					.<ChartBacktestSeries>thenApply(series -> {
						StrategyWithIndicators swi = chartStrategyFactory.build(series, strategyParams);

						BarSeriesManager manager = new BarSeriesManager(series,
								transactionCostModel, new ZeroCostModel());
						TradingRecord tradingRecord = manager.run(swi.strategy());

						List<ChartSeriesMarker> trades = tradingRecord.getTrades().stream()
								.map(trade -> ChartSeriesMarker.of(series.getBar(trade.getIndex()), trade))
								.toList();
						ChartBarSeries chartBarSeries = BarUtils.buildSeries(series, "bars", "bars");

						Map<String, Object> statistics = BacktestUtils.calculateCriterions(series, tradingRecord);

						return ChartBacktestSeries.builder()
								.id(asset.getSymbol())
								.name(asset.getName())
								.lineColor("#030303")
								.values(chartBarSeries.getValues())
								.trades(trades)
								.indicators(swi.indicators())
								.normalDistribution(
										StatisticsUtils.getRealMarketDistribution(series, distributionMa, 10))
								.statistics(statistics)
								.build();
					});
		}).map(CompletableFuture::join).toList();

		return BacktestResult.builder()
				.series(chartSeries)
				.build();
	}

	@PostMapping("{id:\\d+}/optimize")
	public OptimizationResult optimize(
			@PathVariable Integer id,
			@RequestParam String assetId,
			@RequestParam TimeFrame timeFrame,
			@RequestParam Instant startTime,
			@RequestParam Instant endTime,
			@RequestParam Double transactionCost,
			@RequestBody List<OptimizerStrategyParameter> optimizerParams) {

		CostModel transactionCostModel = new LinearTransactionCostModel(transactionCost / 100.0);
		ChartStrategyFactory chartStrategyFactory = new MovingAverageStrategyFactory();
		StrategyOptimizer strategyOptimizer = new ProfitStrategyOptimizer();

		return assetRepository.findBySymbol(assetId)
				.map(asset -> barService.ta4jSeriesAsync(asset.getSymbol(), timeFrame, startTime, endTime, false)
						.thenApply(series -> strategyOptimizer.optimize(
								chartStrategyFactory,
								transactionCostModel,
								series,
								optimizerParams,
								barsExecutor))
						.join())
				.get();
	}
}