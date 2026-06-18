package ru.nuclearius.finam.rest.controller;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.springdoc.core.annotations.ParameterObject;
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
import org.ta4j.core.indicators.averages.SMAIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;

import grpc.tradeapi.v1.marketdata.TimeFrame;
import lombok.RequiredArgsConstructor;
import ru.nuclearius.finam.db.Asset;
import ru.nuclearius.finam.db.Strategy;
import ru.nuclearius.finam.db.StrategyAsset;
import ru.nuclearius.finam.rest.dto.BacktestResult;
import ru.nuclearius.finam.rest.dto.StrategyDto;
import ru.nuclearius.finam.rest.mapper.RESTMapper;
import ru.nuclearius.finam.service.BarService;
import ru.nuclearius.finam.service.StrategyService;

@RestController
@RequestMapping("api/v1/strategies")
@RequiredArgsConstructor
public class StrategyController {
    private final BarService barService;
    private final StrategyService strategyService;
    private final RESTMapper restMapper;

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

    @PostMapping("{id:\\d+}/backtest")
    public BacktestResult backtest(
            @PathVariable Integer id,
            @RequestParam TimeFrame timeFrame,
            @RequestParam Instant startTime,
            @RequestParam Instant endTime) {

        return strategyService.assets(id).stream().map(sa -> {
            Asset asset = sa.getAsset();
            return barService.barsSeriesAsync(asset.getSymbol(), timeFrame, startTime, endTime)
                    .thenApply(series -> {
                        ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
                        SMAIndicator fastSma = new SMAIndicator(closePrice, 5);

                        return BacktestResult.builder().build();
                    });
        }).map(CompletableFuture::join).findFirst().get();
    }
}