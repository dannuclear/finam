package ru.nuclearius.finam.subscriber.orders;

import java.util.HashSet;
import java.util.Set;

import org.springframework.stereotype.Component;

import grpc.tradeapi.v1.orders.OrdersServiceGrpc.OrdersServiceStub;
import grpc.tradeapi.v1.orders.SubscribeOrdersRequest;
import grpc.tradeapi.v1.orders.SubscribeOrdersResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.nuclearius.finam.service.mapper.ProtoMapper;
import ru.nuclearius.finam.subscriber.AbstractBackoffObserver;

@Slf4j
@Component
@RequiredArgsConstructor
public class AccountOrdersSubscriber extends AbstractBackoffObserver<SubscribeOrdersRequest, SubscribeOrdersResponse> {
    private final OrdersServiceStub ordersServiceStub;
    private String accountId = "2029595";
    private final ProtoMapper protoMapper;
    private Set<OrderListener> orderListeners = new HashSet<>();

    @Override
    protected void subscribe() {
        if (accountId == null || accountId.isEmpty())
            return;
        SubscribeOrdersRequest request = SubscribeOrdersRequest.newBuilder()
                .setAccountId(accountId)
                .build();

        ordersServiceStub.subscribeOrders(request, this);
    }

    @Override
    public void onNext(SubscribeOrdersResponse response) {
        response.getOrdersList().stream()
                .map(protoMapper::map)
                .forEach(this::sendForEachListener);
    }

    public void addListener(OrderListener listener) {
        if (!orderListeners.contains(listener))
            orderListeners.add(listener);
    }

    public void removeListener(OrderListener listener) {
        if (!orderListeners.contains(listener))
            orderListeners.remove(listener);
    }

    private void sendForEachListener(ru.nuclearius.finam.service.domain.OrderState order) {
        orderListeners.forEach(listener -> listener.onOrder(order));
    }

    public interface OrderListener {
        void onOrder(ru.nuclearius.finam.service.domain.OrderState order);
    }
}