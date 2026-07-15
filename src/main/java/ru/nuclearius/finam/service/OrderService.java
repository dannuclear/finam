package ru.nuclearius.finam.service;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.nuclearius.finam.service.domain.OrderState;
import ru.nuclearius.finam.service.domain.OrderState.Status;
import ru.nuclearius.finam.subscriber.orders.AccountOrdersSubscriber.OrderListener;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService implements OrderListener {
    private final Map<String, OrderState> orders = new ConcurrentHashMap<>();

    public void save(String id, OrderState orderState) {
        orders.compute(id, (key, old) -> orderState);
    }

    public boolean delete(String id) {
        return orders.remove(id) != null;
    }

    public Collection<OrderState> getAll() {
        return orders.values();
    }

    public Optional<OrderState> findById(String id) {
        return Optional.ofNullable(orders.get(id));
    }

    public Collection<OrderState> findByAsset(String symbol) {
        return orders.values().stream().filter(os -> os.getOrder().getSymbol().equals(symbol)).toList();
    }

    @Override
    public void onOrder(OrderState order) {
        log.info("order: orderId={}, execId={}, Выставлена={}, status={}, symbol={}",
                order.getOrderId(),
                order.getExecId(),
                order.getTransactAt(),
                order.getStatus(),
                order.getOrder().getSymbol());

        Status status = order.getStatus();
        switch (status) {
            case ORDER_STATUS_CANCELED:
            case ORDER_STATUS_FAILED:
                delete(order.getOrderId());
                break;

            default:
                save(order.getOrderId(), order);
                break;
        }
    }
}
