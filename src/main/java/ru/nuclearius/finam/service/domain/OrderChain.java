package ru.nuclearius.finam.service.domain;

import java.util.List;

import lombok.Getter;

@Getter
public class OrderChain {
    private final String id;
    private final List<Order> orders;
    private int index = -1;
    private LinkedOrderStatus status = LinkedOrderStatus.NEW;

    public OrderChain(String id, List<Order> orders) {
        this.id = id;
        this.orders = List.copyOf(orders);
    }

    public synchronized Order next() {
        return orders.get(++index);
    }

    public synchronized boolean hasNext() {
        return index + 1 < orders.size();
    }

    public synchronized boolean isCurrent(String clientOrderId) {
        return index >= 0 && clientOrderId.equals(orders.get(index).getClientOrderId());
    }

    public synchronized void start() {
        status = LinkedOrderStatus.RUNNING;
    }

    public synchronized void complete() {
        status = LinkedOrderStatus.COMPLETED;
    }

    public synchronized void fail() {
        status = LinkedOrderStatus.FAILED;
    }

    public enum LinkedOrderStatus {
        NEW, RUNNING, COMPLETED, FAILED, CANCELED
    }
}