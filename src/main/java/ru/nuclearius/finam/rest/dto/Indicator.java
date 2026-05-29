package ru.nuclearius.finam.rest.dto;

import java.math.BigDecimal;
import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class Indicator {
    private Instant timestamp;

    private BigDecimal value;

    @JsonProperty
    public long mills() {
        return timestamp.toEpochMilli();
    }
}
