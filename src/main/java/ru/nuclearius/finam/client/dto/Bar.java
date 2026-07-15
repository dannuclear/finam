package ru.nuclearius.finam.client.dto;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Bar {
    private Instant timestamp;

    private BigDecimal open;
    private BigDecimal high;
    private BigDecimal low;
    private BigDecimal close;
    private BigDecimal volume;

    @JsonProperty
    public long mills() {
        return timestamp.toEpochMilli();
    }

    @JsonProperty
    public long seconds() {
        return timestamp.getEpochSecond();
    }

    public Bar withPriceOffset(BigDecimal offset) {
        if (offset == null || BigDecimal.ZERO.equals(offset))
            return this;
        this.open = this.open.add(offset);
        this.high = this.high.add(offset);
        this.low = this.low.add(offset);
        this.close = this.close.add(offset);
        return this;
    }

    public Bar withMultiply(BigDecimal factor) {
        if (factor == null)
            return this;
        this.open = this.open.multiply(factor);
        this.high = this.high.multiply(factor);
        this.low = this.low.multiply(factor);
        this.close = this.close.multiply(factor);
        return this;
    }

    public Bar withDivide(BigDecimal value) {
        if (value == null)
            return this;
        this.open = this.open.divide(value, 4, RoundingMode.HALF_UP);
        this.high = this.high.divide(value, 4, RoundingMode.HALF_UP);
        this.low = this.low.divide(value, 4, RoundingMode.HALF_UP);
        this.close = this.close.divide(value, 4, RoundingMode.HALF_UP);
        return this;
    }

    public Bar withPriceOffset(double offset) {
        return withPriceOffset(BigDecimal.valueOf(offset));
    }

    public Bar withMultiply(double value) {
        return withMultiply(BigDecimal.valueOf(value));
    }

    public Bar withDivide(double value) {
        return withDivide(BigDecimal.valueOf(value));
    }

    @Override
    public Bar clone() {
        return new Bar(timestamp, open, high, low, close, volume);
    }
}