package ru.nuclearius.finam.client.dto;

import java.math.BigDecimal;
import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class Quote {

    private String symbol;
    private Instant timestamp;

    private BigDecimal ask;
    private BigDecimal askSize;

    private BigDecimal bid;
    private BigDecimal bidSize;

    private BigDecimal last;
    private BigDecimal lastSize;

    private BigDecimal volume;
    private BigDecimal turnover;

    private BigDecimal open;
    private BigDecimal high;
    private BigDecimal low;
    private BigDecimal close;

    private BigDecimal change;

    private Option option;

    @JsonProperty
    public long mills() {
        return timestamp.toEpochMilli();
    }
}
