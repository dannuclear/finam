package ru.nuclearius.finam.rest.controller;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.ta4j.core.BarSeries;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import ru.nuclearius.finam.rest.dto.ChartIndicatorSeries;
import ru.nuclearius.finam.streamer.AveragePriceSpreadTrader;
import ru.nuclearius.finam.streamer.AveragePriceSpreadTrader.AssetData;
import ru.nuclearius.finam.utils.BarUtils;

@RestController
@RequestMapping("api/v1/spreads")
@RequiredArgsConstructor
public class AveragePriceSpreadController {
    private final AveragePriceSpreadTrader spreadTrader;

    @GetMapping("/symbols")
    public Set<String> symbols() {
        return spreadTrader.getSpreadSymbols();
    }

    @GetMapping("/data")
    public Map<String, ChartIndicatorSeries> data() {
        if (spreadTrader.getAssetIndicators() == null)
            return Map.of();
        return spreadTrader.getAssetIndicators().entrySet().stream()
                .collect(Collectors.<Map.Entry<String, AssetData>, String, ChartIndicatorSeries>toMap(Map.Entry::getKey,
                        e -> {
                            BarSeries barSeries = e.getValue().getBarSeries();
                            return BarUtils.toChartSeries(barSeries, e.getKey(), e.getKey(),
                                    e.getValue().getFastMaIndicator());
                        }));
    }

    @PostMapping("/start")
    public void start(
            @RequestParam Set<String> assets,
            @RequestParam Integer fastMaCount,
            @RequestParam Integer daysCount,
            @RequestParam Double spread) {
        spreadTrader.start(assets, daysCount, fastMaCount, spread);
    }

    @PostMapping("/stop")
    public void stop() {
        spreadTrader.stop();
    }

    @GetMapping("/status")
    public Boolean status() {
        return spreadTrader.isRunning();
    }

    @GetMapping(value = "/subcribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "Subscribe to quotes via SSE")
    @ApiResponse(responseCode = "200", description = "SSE stream of quotes", content = @Content(mediaType = "text/event-stream"))
    public SseEmitter subscribe() {
        SseEmitter emitter = new SseEmitter(0L);
        spreadTrader.subscribe(emitter);
        return emitter;
    }

}
