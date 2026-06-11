package ru.nuclearius.finam.rest.controller;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
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

import grpc.tradeapi.v1.marketdata.TimeFrame;
import lombok.RequiredArgsConstructor;
import ru.nuclearius.finam.client.dto.Bar;
import ru.nuclearius.finam.db.Analysis;
import ru.nuclearius.finam.db.AnalysisAsset;
import ru.nuclearius.finam.rest.dto.AnalysisDTO;
import ru.nuclearius.finam.rest.dto.Series;
import ru.nuclearius.finam.rest.mapper.RESTMapper;
import ru.nuclearius.finam.service.AnalysisService;
import ru.nuclearius.finam.service.FinamService;

@RestController
@RequestMapping("api/v1/analysis")
@RequiredArgsConstructor
public class AnalysisController {
    private final AnalysisService analysisService;
    private final FinamService finamService;
    private final RESTMapper restMapper;

    @GetMapping
    public Page<Analysis> all(
            @RequestParam(required = false) String q,
            @ParameterObject Pageable pageable) {
        return analysisService.findAll(q, pageable);
    }

    @GetMapping("{id:\\d+}")
    public Analysis byId(@PathVariable Integer id) {
        return analysisService.getById(id);
    }

    @GetMapping("{id:\\d+}/assets")
    public List<AnalysisAsset> assets(@PathVariable Integer id) {
        return analysisService.assets(id);
    }

    @PostMapping
    public Analysis create(@RequestBody AnalysisDTO dto) {
        return analysisService.create(dto.getName(), dto.getAssets());
    }

    @PutMapping("{id:\\d+}")
    public Analysis update(@PathVariable Integer id, @RequestBody AnalysisDTO dto) {
        return analysisService.update(id, dto.getName(), dto.getAssets());
    }

    @DeleteMapping("{id:\\d+}")
    public void delete(@PathVariable Integer id) {
        analysisService.delete(id);
    }

    @GetMapping("{id:\\d+}/relative-spreads")
    public List<Series> relativeSpreads(
            @PathVariable Integer id,
            @RequestParam TimeFrame timeFrame,
            @RequestParam Instant startTime,
            @RequestParam Instant endTime,
            @RequestParam TimeFrame averageTimeFrame,
            @RequestParam Instant averageStartTime,
            @RequestParam Instant averageEndTime) {

        var futures = analysisService.assets(id).stream().map(aAsset -> finamService
                .barsAsync(aAsset.getAsset().getSymbol(), averageTimeFrame, averageStartTime, averageEndTime)
                .thenApply(bars -> {
                    DescriptiveStatistics ds = new DescriptiveStatistics();
                    bars.stream()
                            .map(Bar::getClose)
                            .map(BigDecimal::doubleValue)
                            .forEach(ds::addValue);
                    return ds;
                })
                .thenApply(ds -> restMapper.toDto(
                        aAsset,
                        finamService.bars(aAsset.getAsset().getSymbol(), timeFrame, startTime, endTime)
                                .stream()
                                //.map(bar -> bar.withDivide(ds.getMean()).withPriceOffset(-1).withMultiply(100))
                                .map(bar -> bar.withPriceOffset(-ds.getMean()).withDivide(ds.getStandardDeviation()))
                                .toList())))
                .toList();

        return futures.stream().map(CompletableFuture::join).toList();
    }
}
