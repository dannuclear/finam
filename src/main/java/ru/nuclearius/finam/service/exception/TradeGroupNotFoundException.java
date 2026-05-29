package ru.nuclearius.finam.service.exception;

public class TradeGroupNotFoundException extends RuntimeException {
    public TradeGroupNotFoundException() {
        super("Торговый набор не найден");
    }

    public TradeGroupNotFoundException(Integer id) {
        super("Торговый набор с ID: %s не найден".formatted(id));
    }
}
