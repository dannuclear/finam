package ru.nuclearius.finam.service.exception;

public class AnalysisNotFoundException extends RuntimeException {
    public AnalysisNotFoundException() {
        super("Анализ не найден");
    }

    public AnalysisNotFoundException(Integer id) {
        super("Анализ с ID: %s не найден".formatted(id));
    }
}
