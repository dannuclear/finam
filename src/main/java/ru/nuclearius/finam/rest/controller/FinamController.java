package ru.nuclearius.finam.rest.controller;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import grpc.tradeapi.v1.marketdata.TimeFrame;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import ru.nuclearius.finam.client.dto.Account;
import ru.nuclearius.finam.client.dto.Bar;
import ru.nuclearius.finam.client.dto.TokenDetails;
import ru.nuclearius.finam.client.dto.TradeHistory;
import ru.nuclearius.finam.client.dto.TransactionList;
import ru.nuclearius.finam.db.Asset;
import ru.nuclearius.finam.service.BarService;
import ru.nuclearius.finam.service.FinamService;
import ru.nuclearius.finam.service.OrderService;
import ru.nuclearius.finam.service.domain.OrderState;
import ru.nuclearius.finam.streamer.QuoteOrderStreamer;

@RestController
@RequestMapping("api/v1/finam")
@RequiredArgsConstructor
public class FinamController {
    private final FinamService finamService;
    private final BarService barService;
    private final QuoteOrderStreamer quoteStreamer;
    private final OrderService orderService;

    @GetMapping("token-details")
    public TokenDetails tokenDetails() {
        return finamService.getTokenDetails();
    }

    @GetMapping("accounts/{accountId}")
    public Account getAccount(@PathVariable String accountId) {
        return finamService.getAccount(accountId);
    }

    @GetMapping("accounts/{accountId}/trades")
    public TradeHistory accountTrades(
            @PathVariable String accountId,
            @RequestParam(required = false, defaultValue = "10") Integer limit,
            @RequestParam(required = false) OffsetDateTime startTime,
            @RequestParam(required = false) OffsetDateTime endTime) {

        Instant startInstant = startTime != null ? startTime.toInstant() : null;
        Instant endInstant = endTime != null ? endTime.toInstant() : null;

        return finamService.getTrades(accountId, limit, startInstant, endInstant);
    }

    @GetMapping("accounts/{accountId}/transactions")
    public List<TransactionList.Transaction> accountTransactions(
            @PathVariable String accountId,
            @RequestParam(required = false, defaultValue = "10") Integer limit,
            @RequestParam(required = false) OffsetDateTime startTime,
            @RequestParam(required = false) OffsetDateTime endTime) {

        Instant startInstant = startTime != null ? startTime.toInstant() : null;
        Instant endInstant = endTime != null ? endTime.toInstant() : null;

        return finamService.getTransactions(accountId, limit, startInstant, endInstant);
    }

    @GetMapping("assets")
    public Page<Asset> assets(
            @RequestParam(required = false) String q,
            @ParameterObject Pageable pageable) {
        return finamService.assets(q, pageable);
    }

    @GetMapping("assets/{symbol}/bars")
    public List<Bar> bars(
            @PathVariable String symbol,
            @RequestParam TimeFrame timeFrame,
            @RequestParam OffsetDateTime startTime,
            @RequestParam OffsetDateTime endTime) {
        return finamService.bars(symbol, timeFrame, startTime.toInstant(), endTime.toInstant());
    }

    @GetMapping("assets/{symbol}/orders")
    public Collection<OrderState> orders(@PathVariable String symbol) {
        return orderService.findByAsset(symbol);
    }

    @GetMapping("assets/bars")
    public Map<String, List<Bar>> assetsBars(
            @RequestParam Set<String> symbols,
            @RequestParam TimeFrame timeFrame,
            @RequestParam OffsetDateTime startTime,
            @RequestParam OffsetDateTime endTime) {

        List<CompletableFuture<Map.Entry<String, List<Bar>>>> futures = symbols.stream()
                .map(symbol -> {
                    return barService.barsAsync(
                            symbol,
                            timeFrame,
                            startTime.toInstant(),
                            endTime.toInstant())
                            .thenApply(bars -> {
                                return Map.entry(symbol, bars);
                            });
                })
                .toList();

        return futures.stream()
                .map(CompletableFuture::join)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue));
    }

    @GetMapping(value = "assets/{symbol}/subcribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "Subscribe to quotes via SSE")
    @ApiResponse(responseCode = "200", description = "SSE stream of quotes", content = @Content(mediaType = "text/event-stream"))
    public SseEmitter subscribeToAsset(@PathVariable String symbol) {
        SseEmitter emitter = new SseEmitter(0L);
        quoteStreamer.register(symbol, emitter);
        return emitter;
    }
}
