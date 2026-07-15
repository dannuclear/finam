package ru.nuclearius.finam.client.dto;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/**
 * История по сделкам
 */
@Getter
@Setter
public class TradeHistory {

    /**
     * Сделки по аккаунту
     */
    private List<Trade> trades = new ArrayList<>();

    // =========================================================
    // ======================= INNER TYPES ======================
    // =========================================================

    /**
     * Информация о сделке
     */
    @Getter
    @Setter
    public static class Trade {

        /** Идентификатор сделки */
        @NotNull
        private String tradeId;

        /** Символ инструмента */
        @NotNull
        private String symbol;

        /**
         * Цена сделки
         * Decimal из схемы представлен как BigDecimal
         */
        private BigDecimal price;

        /**
         * Размер сделки
         */
        private BigDecimal size;

        /**
         * Сторона сделки
         */
        private Side side = Side.SIDE_UNSPECIFIED;

        /**
         * Метка времени
         */
        private Instant timestamp;

        /** Идентификатор заявки */
        private String orderId;

        /** Идентификатор аккаунта */
        private String accountId;

        /** Комментарий (до 128 символов) */
        private String comment;

        public Trade withPriceOffset(BigDecimal offset) {
            if (offset == null || BigDecimal.ZERO.equals(offset))
                return this;
            this.price = this.price.add(offset);
            return this;
        }

        public Trade withMultiply(BigDecimal factor) {
            if (factor == null)
                return this;
            this.price = this.price.multiply(factor);
            return this;
        }

        public Trade withDivide(BigDecimal value) {
            if (value == null)
                return this;
            this.price = this.price.divide(value, 4, RoundingMode.HALF_UP);
            return this;
        }

        public Trade withDivide(double value) {
            return withDivide(BigDecimal.valueOf(value));
        }

        public Trade withPriceOffset(double offset) {
            return withPriceOffset(BigDecimal.valueOf(offset));
        }

        public Trade withMultiply(double value) {
            return withMultiply(BigDecimal.valueOf(value));
        }

        @JsonGetter("sum")
        public BigDecimal sum() {
            if (size == null || price == null)
                return null;
            return size.multiply(price).setScale(2, RoundingMode.HALF_UP);
        }

        @JsonProperty
        public long mills() {
            return timestamp.toEpochMilli();
        }

        @JsonProperty
        public long seconds() {
            return timestamp.getEpochSecond();
        }
    }

    /**
     * Сторона сделки
     */
    public enum Side {

        /** Сторона не указана */
        SIDE_UNSPECIFIED,

        /** Покупка */
        SIDE_BUY,

        /** Продажа */
        SIDE_SELL,

        UNRECOGNIZED
    }
}
