package ru.nuclearius.finam.strategy.optimizer.parameters;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ValuesOptimizerStrategyParameter extends OptimizerStrategyParameter {
    public ValuesOptimizerStrategyParameter(String id, List<String> options) {
        super(id);
        this.options = options;
    }

    private List<String> options;

    @Override
    public List<?> getValues() {
        return options;
    }
}