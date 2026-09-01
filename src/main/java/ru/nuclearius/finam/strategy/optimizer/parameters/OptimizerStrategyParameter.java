package ru.nuclearius.finam.strategy.optimizer.parameters;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
@JsonTypeInfo(use = JsonTypeInfo.Id.DEDUCTION)
@JsonSubTypes({
        @JsonSubTypes.Type(RangeOptimizerStrategyParameter.class),
        @JsonSubTypes.Type(ValuesOptimizerStrategyParameter.class)
})
@Schema(oneOf = { RangeOptimizerStrategyParameter.class, ValuesOptimizerStrategyParameter.class })
public abstract class OptimizerStrategyParameter {
    private final String id;

    @JsonIgnore
    public abstract List<?> getValues();
}