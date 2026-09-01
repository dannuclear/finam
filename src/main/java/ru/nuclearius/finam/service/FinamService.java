package ru.nuclearius.finam.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.util.Pair;
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
import grpc.tradeapi.v1.orders.Order;
import grpc.tradeapi.v1.orders.OrdersServiceGrpc.OrdersServiceBlockingStub;
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
import ru.nuclearius.finam.service.domain.Order.Side;
import ru.nuclearius.finam.service.domain.Order.StopCondition;
import ru.nuclearius.finam.service.domain.Order.TimeInForce;
import ru.nuclearius.finam.service.domain.Order.Type;
import ru.nuclearius.finam.service.domain.OrderState;
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
    private final OrdersServiceBlockingStub ordersServiceBlockingStub;
    private final ProtoMapper protoMapper;

    private final AssetRepository assetRepository;

    public TokenDetails getTokenDetails() {
        if (!jwtTokenHolder.hasToken())
            return null;
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
     * Выставляет биржевую заявку через торговый API.
     *
     * @param accountId   идентификатор торгового счета
     * @param symbol      символ инструмента
     * @param quantity    количество инструмента в штуках
     * @param side        направление заявки
     * @param type        тип заявки
     * @param timeInForce срок действия заявки
     * @param limitPrice  лимитная цена (для LIMIT и STOP_LIMIT заявок)
     * @param stopPrice   стоп-цена (для STOP и STOP_LIMIT заявок)
     *
     * @return состояние выставленной заявки
     */
    public OrderState placeOrder(
            String accountId,
            String symbol,
            String clientOrderId,
            BigDecimal quantity,
            Side side,
            Type type,
            TimeInForce timeInForce,
            BigDecimal limitPrice,
            BigDecimal stopPrice,
            StopCondition stopCondition) {

        if (quantity == null || quantity.signum() <= 0)
            throw new IllegalArgumentException("Order quantity must be greater than zero");

        if (side == null || side == Side.SIDE_UNSPECIFIED)
            throw new IllegalArgumentException("Order side must be specified");

        if (type == null || type == Type.ORDER_TYPE_UNSPECIFIED)
            throw new IllegalArgumentException("Order type must be specified");

        if (timeInForce == null || timeInForce == TimeInForce.TIME_IN_FORCE_UNSPECIFIED)
            throw new IllegalArgumentException("Time in force must be specified");

        switch (type) {
            case ORDER_TYPE_LIMIT -> {
                if (limitPrice == null)
                    throw new IllegalArgumentException("Limit price is required for LIMIT order");
            }

            case ORDER_TYPE_STOP -> {
                if (stopPrice == null)
                    throw new IllegalArgumentException("Stop price is required for STOP order");
            }
            case ORDER_TYPE_STOP_LIMIT -> {
                if (stopPrice == null)
                    throw new IllegalArgumentException("Stop price is required for STOP_LIMIT order");
                if (limitPrice == null)
                    throw new IllegalArgumentException("Limit price is required for STOP_LIMIT order");
            }

            default -> {
                // MARKET и остальные типы дополнительных параметров не требуют
            }
        }

        if (limitPrice != null && limitPrice.signum() <= 0)
            throw new IllegalArgumentException("Limit price must be greater than zero");

        if (stopPrice != null && stopPrice.signum() <= 0)
            throw new IllegalArgumentException("Stop price must be greater than zero");

        Order.Builder builder = Order.newBuilder()
                .setAccountId(accountId)
                .setSymbol(symbol)
                .setClientOrderId(clientOrderId)
                .setSide(protoMapper.map(side))
                .setType(protoMapper.map(type))
                .setTimeInForce(protoMapper.map(timeInForce))
                .setQuantity(protoMapper.map(quantity));

        if (limitPrice != null) {
            builder.setLimitPrice(protoMapper.map(limitPrice));
        }

        if (stopPrice != null) {
            builder.setStopPrice(protoMapper.map(stopPrice));
        }

        if (stopCondition != null) {
            builder.setStopCondition(protoMapper.map(stopCondition));
        }

        return protoMapper.map(ordersServiceBlockingStub.placeOrder(builder.build()));
    }

    /**
     * Создает лимитную заявку.
     *
     * <p>
     * Лимитная заявка требует указания цены. Цена должна быть
     * положительным числом.
     * </p>
     *
     * @param accountId   идентификатор счета
     * @param symbol      инструмент (например, SBER@MISX)
     * @param quantity    количество ценных бумаг
     * @param side        направление заявки (BUY/SELL)
     * @param price       лимитная цена
     * @param timeInForce срок действия заявки
     * @return состояние выставленной заявки
     * @throws IllegalArgumentException если цена или количество некорректны
     */
    public OrderState limitOrder(
            String accountId,
            String symbol,
            String clientOrderId,
            BigDecimal quantity,
            Side side,
            BigDecimal price,
            TimeInForce timeInForce) {

        if (quantity == null || quantity.signum() <= 0)
            throw new IllegalArgumentException("Order quantity must be greater than zero");

        if (price == null || price.signum() <= 0)
            throw new IllegalArgumentException("Limit price must be greater than zero");

        return placeOrder(
                accountId,
                symbol,
                clientOrderId,
                quantity,
                side,
                Type.ORDER_TYPE_LIMIT,
                timeInForce,
                price,
                null,
                null);
    }

    /**
     * Создает и отправляет рыночную заявку.
     *
     * @param accountId   идентификатор счета
     * @param symbol      инструмент
     * @param quantity    количество
     * @param side        сторона заявки
     * @param timeInForce срок действия заявки
     * @return состояние заявки
     */
    public OrderState marketOrder(
            String accountId,
            String symbol,
            String clientOrderId,
            BigDecimal quantity,
            Side side,
            TimeInForce timeInForce) {

        if (quantity == null || quantity.signum() <= 0)
            throw new IllegalArgumentException("Order quantity must be greater than zero");

        return placeOrder(
                accountId,
                symbol,
                clientOrderId,
                quantity,
                side,
                Type.ORDER_TYPE_MARKET,
                timeInForce,
                null,
                null,
                null);
    }

    /**
     * Создает стоп-рыночную заявку.
     *
     * @param accountId   идентификатор счета
     * @param symbol      инструмент
     * @param quantity    количество
     * @param side        сторона заявки
     * @param stopPrice   цена активации стопа
     * @param timeInForce срок действия
     * @return состояние заявки
     */
    public OrderState stopOrder(
            String accountId,
            String symbol,
            String clientOrderId,
            BigDecimal quantity,
            Side side,
            BigDecimal stopPrice,
            TimeInForce timeInForce) {

        if (quantity == null || quantity.signum() <= 0)
            throw new IllegalArgumentException("Order quantity must be greater than zero");
        if (stopPrice == null || stopPrice.signum() <= 0)
            throw new IllegalArgumentException("Stop price must be greater than zero");

        return placeOrder(
                accountId,
                symbol,
                clientOrderId,
                quantity,
                side,
                Type.ORDER_TYPE_STOP,
                timeInForce,
                null,
                stopPrice,
                null);
    }

    /**
     * Создает стоп-лимитную заявку.
     *
     * @param accountId   идентификатор счета
     * @param symbol      инструмент
     * @param quantity    количество
     * @param side        сторона заявки
     * @param stopPrice   цена активации стопа
     * @param limitPrice  цена лимитной заявки после активации
     * @param timeInForce срок действия
     * @return состояние заявки
     */
    public OrderState stopLimitOrder(
            String accountId,
            String symbol,
            String clientOrderId,
            BigDecimal quantity,
            Side side,
            BigDecimal stopPrice,
            BigDecimal limitPrice,
            StopCondition stopCondition,
            TimeInForce timeInForce) {

        if (quantity == null || quantity.signum() <= 0)
            throw new IllegalArgumentException("Order quantity must be greater than zero");
        if (stopPrice == null || stopPrice.signum() <= 0)
            throw new IllegalArgumentException("Stop price must be greater than zero");
        if (limitPrice == null || limitPrice.signum() <= 0)
            throw new IllegalArgumentException("Limit price must be greater than zero");

        return placeOrder(
                accountId,
                symbol,
                clientOrderId,
                quantity,
                side,
                Type.ORDER_TYPE_STOP_LIMIT,
                timeInForce,
                limitPrice,
                stopPrice,
                stopCondition);
    }

    public Map<String, List<TradeHistory.Trade>> getTradesGroups(
            String accountId,
            Integer limit,
            Instant startTime,
            Instant endTime) {
        TradeHistory tradeHistory = getTrades(accountId, limit, startTime, endTime);

        return tradeHistory.getTrades().stream()
                .collect(Collectors.groupingBy(
                        TradeHistory.Trade::getSymbol,
                        LinkedHashMap::new,
                        Collectors.collectingAndThen(
                                Collectors.toList(),
                                list -> new ArrayList<>(
                                        list.stream()
                                                .collect(Collectors.toMap(
                                                        trade -> Pair.of(trade.getTimestamp(), trade.getSide()),
                                                        Function.identity(),
                                                        (t1, t2) -> {
                                                            BigDecimal totalSize = t1.getSize().add(t2.getSize());

                                                            BigDecimal totalAmount = t1.getPrice()
                                                                    .multiply(t1.getSize())
                                                                    .add(t2.getPrice().multiply(t2.getSize()));

                                                            BigDecimal averagePrice = totalAmount.divide(
                                                                    totalSize,
                                                                    10,
                                                                    RoundingMode.HALF_UP);

                                                            t1.setSize(totalSize);
                                                            t1.setPrice(averagePrice);

                                                            return t1;
                                                        },
                                                        LinkedHashMap::new))
                                                .values()))));
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
    @Cacheable(value = "bars", key = "{#symbol, #timeFrame.name(), #startTime, #endTime}")
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
}