package ru.nuclearius.finam.rest.controller;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.coyote.BadRequestException;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.util.CollectionUtils;
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
import ru.nuclearius.finam.db.Asset;
import ru.nuclearius.finam.db.TradeGroup;
import ru.nuclearius.finam.db.TradeGroupReferenceAsset;
import ru.nuclearius.finam.db.TradeGroupTradedAsset;
import ru.nuclearius.finam.repository.TradeGroupReferenceAssetRepository;
import ru.nuclearius.finam.rest.dto.ChartBarSeries;
import ru.nuclearius.finam.rest.dto.TradeGroupDTO;
import ru.nuclearius.finam.rest.mapper.RESTMapper;
import ru.nuclearius.finam.service.BarService;
import ru.nuclearius.finam.service.TradeGroupService;
import ru.nuclearius.finam.service.mapper.EntityMapper;

@RestController
@RequestMapping("api/v1/trade-groups")
@RequiredArgsConstructor
public class TradeGroupController {
    private final TradeGroupService tradeGroupService;
    private final EntityMapper entityMapper;
    // private final SubscriptionManager subscriptionManager;
    // private final QuoteSubscriberFactory quoteSubscriberFactory;
    private final BarService barService;
    private final RESTMapper restMapper;

    private final TradeGroupReferenceAssetRepository tradeGroupReferenceAssetRepository;

    @GetMapping
    public Page<TradeGroup> all(
            @RequestParam(required = false) String q,
            @ParameterObject Pageable pageable) {
        return tradeGroupService.findAll(q, pageable);
    }

    @GetMapping("{id:\\d+}")
    public TradeGroupDTO byId(@PathVariable Integer id) {
        return entityMapper.toDTO(tradeGroupService.getById(id));
    }

    @PostMapping
    public TradeGroup create(@RequestBody TradeGroupDTO dto) {
        return tradeGroupService.create(dto.getName(), dto.getDescription(), dto.getTradedAssets(),
                dto.getReferenceAssets());
    }

    @PutMapping("path/{id:\\d+}")
    public TradeGroup update(@PathVariable Integer id, @RequestBody TradeGroupDTO dto) {
        return tradeGroupService.update(id, dto.getName(), dto.getDescription(), dto.getTradedAssets(),
                dto.getReferenceAssets());
    }

    @GetMapping("{id:\\d+}/traded-assets")
    public List<TradeGroupTradedAsset> tradedAssets(@PathVariable Integer id) {
        return tradeGroupService.tradedAssetsByGroupId(id);
    }

    @GetMapping("{id:\\d+}/reference-assets")
    public List<TradeGroupReferenceAsset> referenceAssets(@PathVariable Integer id) {
        return tradeGroupService.referenceAssetsByGroupId(id);
    }

    @PostMapping("{id:\\d+}/toggle-active")
    public Boolean toggleActiveObserver(@PathVariable Integer id) throws BadRequestException {
        TradeGroup tradeGroup = tradeGroupService.getById(id);
        List<TradeGroupTradedAsset> tradedAssets = tradeGroupService.tradedAssetsByGroupId(id);
        List<TradeGroupReferenceAsset> referenceAssets = tradeGroupService.referenceAssetsByGroupId(id);
        if (CollectionUtils.isEmpty(tradedAssets) || CollectionUtils.isEmpty(referenceAssets))
            throw new BadRequestException("Торговые инструменты на заданы");

        Set<String> symbols = Stream.concat(
                tradedAssets.stream().map(TradeGroupTradedAsset::getAsset),
                referenceAssets.stream().map(TradeGroupReferenceAsset::getAsset))
                .map(Asset::getSymbol).collect(Collectors.toSet());

        String tradeGroupKey = tradeGroupKey(id);
        // if (subscriptionManager.isRunning(tradeGroupKey)) {
        //     subscriptionManager.stop(tradeGroupKey);
        // } else {
            // QuoteSubscriber quoteSubscriber = quoteSubscriberFactory.create((quote) -> {
            // System.out.println(quote);
            // }, () -> symbols);
            // subscriptionManager.register(tradeGroupKey, quoteSubscriber);
            // quoteSubscriber.start();
        // }
        return true;
    }

    @PostMapping("{id:\\d+}/calculate-reference-offsets")
    public void calculateReferenceOffsets(@PathVariable Integer id,
            @RequestParam TimeFrame timeFrame,
            @RequestParam OffsetDateTime startTime,
            @RequestParam OffsetDateTime endTime) {
        var results = tradeGroupService.referenceAssetsByGroupId(id).stream()
                .filter(asset -> asset.getEnabled())
                .map(asset -> barService.barsAsync(
                        asset.getAsset().getSymbol(),
                        timeFrame,
                        startTime.toInstant(),
                        endTime.toInstant())
                        .thenApply(bars -> Pair.of(
                                asset.getId(),
                                bars.stream()
                                        .map(Bar::getClose)
                                        .reduce(BigDecimal.ZERO, BigDecimal::add)
                                        .divide(BigDecimal.valueOf(bars.size()), RoundingMode.HALF_UP))))
                .toList();

        var values = results.stream()
                .map(CompletableFuture::join)
                .toList();

        BigDecimal groupAverage = values.stream()
                .map(Pair::getRight)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(values.size()), RoundingMode.HALF_UP);

        values.stream()
                .forEach(pair -> tradeGroupReferenceAssetRepository.updatePriceOffsetById(
                        pair.getLeft(),
                        groupAverage.subtract(pair.getRight())));
    }

    @GetMapping("{id:\\d+}/reference-assets/bars")
    public List<ChartBarSeries> referenceAssetsBars(@PathVariable Integer id,
            @RequestParam TimeFrame timeFrame,
            @RequestParam OffsetDateTime startTime,
            @RequestParam OffsetDateTime endTime,
            @RequestParam(defaultValue = "30", required = false) Integer smaBarCount) {

        return Collections.emptyList();
    }

    private String tradeGroupKey(Integer id) {
        return "trade-group-%d".formatted(id);
    }
}
