package ru.nuclearius.finam.strategy.factory;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.ta4j.core.BarSeries;

import ru.nuclearius.finam.strategy.StrategyWithIndicators;

public abstract class ChartStrategyFactory {

    public abstract List<StrategyParameter> getParameters();

    public abstract StrategyWithIndicators build(BarSeries series, Map<String, String> values);

    protected <T> T getParameter(Map<String, String> values, String name, Class<T> type) {
        String value = values.get(name);

        if (value == null) {
            throw new IllegalArgumentException("Parameter '%s' is required".formatted(name));
        }

        if (type == String.class) {
            return type.cast(value);
        }

        if (type == Integer.class) {
            return type.cast(new BigDecimal(value).intValueExact());
        }

        if (type == Long.class) {
            return type.cast(Long.valueOf(value));
        }

        if (type == Double.class) {
            return type.cast(Double.valueOf(value));
        }

        if (type == Float.class) {
            return type.cast(Float.valueOf(value));
        }

        if (type == Boolean.class) {
            return type.cast(Boolean.valueOf(value));
        }

        if (type == Short.class) {
            return type.cast(Short.valueOf(value));
        }

        if (type == Byte.class) {
            return type.cast(Byte.valueOf(value));
        }

        if (type == Enum.class) {
            throw new IllegalArgumentException("Enum type must be specified explicitly");
        }

        throw new IllegalArgumentException("Unsupported parameter type: %s".formatted(type.getName()));
    }
}