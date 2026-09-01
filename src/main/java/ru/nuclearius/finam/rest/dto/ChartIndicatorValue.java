package ru.nuclearius.finam.rest.dto;

import java.math.BigDecimal;
import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class ChartIndicatorValue {
    private Instant timestamp;

    private BigDecimal value;

    @JsonProperty
    public Long seconds() {
        return timestamp != null ? timestamp.getEpochSecond() : null;
    }
}
