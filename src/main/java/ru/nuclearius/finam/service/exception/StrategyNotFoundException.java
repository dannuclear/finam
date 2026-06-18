package ru.nuclearius.finam.service.exception;

public class StrategyNotFoundException extends RuntimeException {
    public StrategyNotFoundException() {
        super("Стратегия не найдена");
    }

    public StrategyNotFoundException(Integer id) {
        super("Стратегия с ID: %s не найдена".formatted(id));
    }
}
