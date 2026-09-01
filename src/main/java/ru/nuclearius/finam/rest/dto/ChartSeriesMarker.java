package ru.nuclearius.finam.rest.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

import org.ta4j.core.Bar;
import org.ta4j.core.Trade;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;

@Getter
public class ChartSeriesMarker {
    private final Instant timestamp;

    private final Action action;
    private final BigDecimal price;

    @JsonProperty
    public Long seconds() {
        return timestamp != null ? timestamp.getEpochSecond() : null;
    }

    private ChartSeriesMarker(Instant timestamp, Action action, BigDecimal price) {
        Objects.requireNonNull(timestamp);
        Objects.requireNonNull(action);
        this.timestamp = timestamp;
        this.action = action;
        this.price = price;
    }

    public static ChartSeriesMarker of(Instant timestamp, Action action, BigDecimal price) {
        return new ChartSeriesMarker(timestamp, action, price);
    }

    public static ChartSeriesMarker of(Bar bar, Trade trade) {
        return of(
                bar.getEndTime(),
                trade.isBuy() ? Action.BUY : Action.SELL,
                trade.getNetPrice().bigDecimalValue());
    }

    public static enum Action {
        BUY, SELL
    }
}
