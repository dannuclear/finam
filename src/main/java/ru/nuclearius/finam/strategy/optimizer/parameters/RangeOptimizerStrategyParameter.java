package ru.nuclearius.finam.strategy.optimizer.parameters;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RangeOptimizerStrategyParameter extends OptimizerStrategyParameter {

    public RangeOptimizerStrategyParameter(String id, Number from, Number to, Number step) {
        super(id);
        this.from = from;
        this.to = to;
        this.step = step;
    }

    private Number from;
    private Number to;
    private Number step;

    @Override
    public List<?> getValues() {
        List<String> values = new ArrayList<>();
        for (double value = from.doubleValue(); value <= to.doubleValue(); value += step.doubleValue()) {
            values.add(String.format(Locale.US, "%.2f", value));
        }
        return values;
    }
}