package ru.nuclearius.finam.strategy.factory;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@AllArgsConstructor
@RequiredArgsConstructor
public class StrategyParameter {
    private final String id;
    private final String name;
    private final Type type;
    private Map<String, String> options;

    public static enum Type {
        INTEGER, DOUBLE, STRING, ENUM
    }
}