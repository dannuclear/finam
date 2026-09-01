package ru.nuclearius.finam.service;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.nuclearius.finam.service.domain.Order;
import ru.nuclearius.finam.service.domain.OrderChain;
import ru.nuclearius.finam.service.domain.OrderState;
import ru.nuclearius.finam.subscriber.orders.AccountOrdersSubscriber.OrderListener;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService implements OrderListener {

    private static final String ACCOUNT_ID = "2029595";

    private final FinamService finamService;

    private final Map<String, OrderState> orders = new ConcurrentHashMap<>();
    private final Map<String, OrderChain> chains = new ConcurrentHashMap<>();
    private final Map<String, String> clientOrderToChain = new ConcurrentHashMap<>();

    public Boolean hasChains (){
        return !clientOrderToChain.isEmpty();
    }

    public void save(String id, OrderState state) {
        orders.put(id, state);
    }

    public Optional<OrderState> findById(String id) {
        return Optional.ofNullable(orders.get(id));
    }

    public Collection<OrderState> getAll() {
        return orders.values();
    }

    public Collection<OrderState> findByAsset(String symbol) {
        return orders.values().stream()
                .filter(o -> symbol.equals(o.getOrder().getSymbol()))
                .toList();
    }

    public String createChain(List<Order> orders) {
        String id = UUID.randomUUID().toString()
                .replace("-", "")
                .substring(0, 10);

        for (int i = 0; i < orders.size(); i++) {
            orders.get(i).setClientOrderId(id + "num" + i);
        }

        OrderChain chain = new OrderChain(id, orders);
        chains.put(id, chain);
        executeNext(chain);

        return id;
    }

    private void executeNext(OrderChain chain) {
        synchronized (chain) {
            if (!chain.hasNext()) {
                chain.complete();
                return;
            }

            Order order = chain.next();
            chain.start();

            clientOrderToChain.put(
                    order.getClientOrderId(),
                    chain.getId());

            finamService.placeOrder(
                    ACCOUNT_ID,
                    order.getSymbol(),
                    order.getClientOrderId(),
                    order.getQuantity(),
                    order.getSide(),
                    order.getType(),
                    order.getTimeInForce(),
                    order.getLimitPrice(),
                    order.getStopPrice(),
                    order.getStopCondition());
        }
    }

    @Override
    public void onOrder(OrderState order) {
        save(order.getOrderId(), order);

        String clientOrderId = order.getOrder().getClientOrderId();
        String chainId = clientOrderToChain.get(clientOrderId);

        if (chainId == null)
            return;

        OrderChain chain = chains.get(chainId);

        if (chain == null || !chain.isCurrent(clientOrderId))
            return;

        switch (order.getStatus()) {
            case ORDER_STATUS_FILLED,
                    ORDER_STATUS_EXECUTED ->
                onFilled(chain, clientOrderId);

            case ORDER_STATUS_CANCELED,
                    ORDER_STATUS_FAILED,
                    ORDER_STATUS_REJECTED,
                    ORDER_STATUS_REJECTED_BY_EXCHANGE,
                    ORDER_STATUS_DENIED_BY_BROKER,
                    ORDER_STATUS_EXPIRED ->
                onFailed(chain, clientOrderId);

            case ORDER_STATUS_PARTIALLY_FILLED ->
                log.info(
                        "Partial fill: orderId={}, executed={}, remaining={}",
                        order.getOrderId(),
                        order.getExecutedQuantity(),
                        order.getRemainingQuantity());

            default -> {
            }
        }
    }

    private void onFilled(OrderChain chain, String clientOrderId) {
        clientOrderToChain.remove(clientOrderId, chain.getId());

        if (chain.hasNext())
            executeNext(chain);
        else
            chain.complete();
    }

    private void onFailed(OrderChain chain, String clientOrderId) {
        clientOrderToChain.remove(clientOrderId, chain.getId());
        chain.fail();
    }
}
