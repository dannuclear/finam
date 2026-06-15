package ru.nuclearius.finam.service;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import grpc.tradeapi.v1.accounts.AccountsServiceGrpc;
import grpc.tradeapi.v1.accounts.GetAccountRequest;
import grpc.tradeapi.v1.accounts.GetAccountResponse;
import grpc.tradeapi.v1.accounts.TradesRequest;
import grpc.tradeapi.v1.accounts.TradesResponse;
import grpc.tradeapi.v1.accounts.TransactionsRequest;
import grpc.tradeapi.v1.accounts.TransactionsResponse;
import grpc.tradeapi.v1.assets.AllAssetsRequest;
import grpc.tradeapi.v1.assets.AllAssetsResponse;
import grpc.tradeapi.v1.assets.AssetsServiceGrpc;
import grpc.tradeapi.v1.assets.GetAssetRequest;
import grpc.tradeapi.v1.assets.GetAssetResponse;
import grpc.tradeapi.v1.auth.AuthServiceGrpc.AuthServiceBlockingStub;
import grpc.tradeapi.v1.auth.TokenDetailsRequest;
import grpc.tradeapi.v1.auth.TokenDetailsResponse;
import grpc.tradeapi.v1.marketdata.BarsRequest;
import grpc.tradeapi.v1.marketdata.BarsResponse;
import grpc.tradeapi.v1.marketdata.MarketDataServiceGrpc.MarketDataServiceBlockingStub;
import grpc.tradeapi.v1.marketdata.TimeFrame;
import io.micrometer.common.util.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.nuclearius.finam.client.dto.Account;
import ru.nuclearius.finam.client.dto.AssetInfo;
import ru.nuclearius.finam.client.dto.Bar;
import ru.nuclearius.finam.client.dto.TokenDetails;
import ru.nuclearius.finam.client.dto.TradeHistory;
import ru.nuclearius.finam.client.dto.TransactionList.Transaction;
import ru.nuclearius.finam.db.Asset;
import ru.nuclearius.finam.grpc.JwtTokenHolder;
import ru.nuclearius.finam.repository.AssetRepository;
import ru.nuclearius.finam.service.domain.BarsWithDescriptiveStatistics;
import ru.nuclearius.finam.service.mapper.ProtoMapper;
import ru.nuclearius.finam.utils.DateUtils;
import ru.nuclearius.finam.utils.TimeFrameUtils;

@Service
@Slf4j
@RequiredArgsConstructor
public class FinamService {
    private final JwtTokenHolder jwtTokenHolder;
    private final AccountsServiceGrpc.AccountsServiceBlockingStub grpcAccountsService;
    private final AssetsServiceGrpc.AssetsServiceBlockingStub grpcAssetsService;
    private final AuthServiceBlockingStub authServiceBlockingStub;
    private final MarketDataServiceBlockingStub marketDataService;
    private final ProtoMapper protoMapper;

    private final AssetRepository assetRepository;

    public TokenDetails getTokenDetails() {
        TokenDetailsResponse response = authServiceBlockingStub.tokenDetails(TokenDetailsRequest.newBuilder()
                .setToken(jwtTokenHolder.getToken())
                .build());
        return protoMapper.toDomain(response);
    }

    /**
     * Получение информации по конкретному аккаунту
     *
     * @param accountId Идентификатор аккаунта
     * @return Mono с информацией о счете
     */
    public Account getAccount(String accountId) {
        Assert.hasText(accountId, "Account id must be present");
        GetAccountResponse response = grpcAccountsService.getAccount(GetAccountRequest.newBuilder()
                .setAccountId(accountId)
                .build());
        return protoMapper.toDomain(response);
    }

    /**
     * Получение истории по сделкам аккаунта
     *
     * @param accountId Идентификатор аккаунта
     * @param limit     Лимит количества сделок
     * @param interval  Период выборки (google.type.Interval)
     * @return Mono с историей сделок
     */
    public TradeHistory getTrades(
            String accountId,
            Integer limit,
            Instant startTime,
            Instant endTime) {
        Assert.hasText(accountId, "Account id must be present");
        Assert.notNull(endTime, "End Time id required");
        TradesRequest request = TradesRequest.newBuilder()
                .setAccountId(accountId)
                .setInterval(DateUtils.toInterval(startTime, endTime))
                .setLimit(limit)
                .build();
        TradesResponse response = grpcAccountsService.trades(request);
        return protoMapper.toDomain(response);
    }

    /**
     * Получение списка транзакций аккаунта
     *
     * @param accountId Идентификатор аккаунта
     * @param limit     Лимит количества транзакций
     * @param interval  Период выборки (google.type.Interval)
     * @return Список с транзакциями аккаунта
     */
    public List<Transaction> getTransactions(
            String accountId,
            Integer limit,
            Instant startTime,
            Instant endTime) {
        Assert.hasText(accountId, "Account id must be present");
        Assert.notNull(endTime, "End Time id required");
        TransactionsRequest request = TransactionsRequest.newBuilder()
                .setAccountId(accountId)
                .setInterval(DateUtils.toInterval(startTime, endTime))
                .setLimit(limit)
                .build();
        TransactionsResponse response = grpcAccountsService.transactions(request);
        return protoMapper.toDomain(response).getTransactions();
    }

    public Page<Asset> assets(String q, Pageable pageable) {
        if (assetRepository.count() == 0) {
            AllAssetsResponse response = null;
            for (long cursor = 0;; cursor = response.getNextCursor()) {
                AllAssetsRequest request = AllAssetsRequest.newBuilder()
                        .setCursor(cursor)
                        .setOnlyActive(true)
                        .build();

                response = grpcAssetsService.allAssets(request);
                List<Asset> assets = protoMapper.toDomain(response).getAssets();
                assetRepository.saveAll(assets);
                if (response.getNextCursor() < cursor)
                    break;
            }
        }
        if (StringUtils.isEmpty(q))
            return assetRepository.findAll(pageable);
        return assetRepository.search(q, pageable);

    }

    public AssetInfo assetInfo(String symbol, String accountId) {
        GetAssetRequest request = GetAssetRequest.newBuilder()
                .setAccountId(accountId)
                .setSymbol(symbol)
                .build();
        GetAssetResponse response = grpcAssetsService.getAsset(request);
        return protoMapper.toDomain(response);
    }

    /**
     * Получение исторических данных по инструменту (агрегированные свечи)
     *
     * @param symbol    Символ инструмента
     * @param timeFrame Необходимый таймфрейм
     * @param startTime Optional. Inclusive start of the interval. If specified, a
     *                  Timestamp matching this interval will have to be the same or
     *                  after the start.
     * @param endTime   Optional. Exclusive end of the interval. If specified, a
     *                  Timestamp matching this interval will have to be before the
     *                  end.
     * @return Агрегированные свечи
     */
    public List<Bar> bars(
            String symbol,
            TimeFrame timeFrame,
            Instant startTime,
            Instant endTime) {

        TimeFrameUtils.validateTimeFrameDepth(timeFrame, startTime, endTime);
        BarsRequest barsRequest = BarsRequest.newBuilder()
                .setSymbol(symbol)
                .setTimeframe(timeFrame)
                .setInterval(DateUtils.toInterval(startTime, endTime))
                .build();
        log.debug("Start retrieve bars for: {} with thread {}", symbol, Thread.currentThread().getName());
        Instant start = Instant.now();
        BarsResponse response = marketDataService
                .withDeadlineAfter(6, TimeUnit.SECONDS)
                .bars(barsRequest);
        Instant end = Instant.now();
        long durationMillis = Duration.between(start, end).toMillis();
        log.debug("Finish retrieve bars for: {} (took {} ms)", symbol, durationMillis);
        return protoMapper.toDomain(response).getBars();
    }

    /**
     * Получение исторических данных по инструменту (агрегированные свечи)
     * асинхронно
     *
     * @param symbol    Символ инструмента
     * @param timeFrame Необходимый таймфрейм
     * @param startTime Optional. Inclusive start of the interval. If specified, a
     *                  Timestamp matching this interval will have to be the same or
     *                  after the start.
     * @param endTime   Optional. Exclusive end of the interval. If specified, a
     *                  Timestamp matching this interval will have to be before the
     *                  end.
     * @return Future Агрегированные свечи
     */
    @Async("barsExecutor")
    public CompletableFuture<List<Bar>> barsAsync(
            String symbol,
            TimeFrame timeFrame,
            Instant startTime,
            Instant endTime) {
        List<Bar> bars = bars(symbol, timeFrame, startTime, endTime);
        return CompletableFuture.completedFuture(bars);
    }

    @Async("barsExecutor")
    public CompletableFuture<BarsWithDescriptiveStatistics> barsWithDescriptiveStatisticsAsync(
            String symbol,
            TimeFrame timeFrame,
            Instant startTime,
            Instant endTime) {
        List<Bar> bars = bars(symbol, timeFrame, startTime, endTime);
        DescriptiveStatistics ds = new DescriptiveStatistics();
        bars.stream()
                .map(Bar::getClose)
                .map(BigDecimal::doubleValue)
                .forEach(ds::addValue);
        return CompletableFuture.completedFuture(new BarsWithDescriptiveStatistics(bars, ds));
    }
}