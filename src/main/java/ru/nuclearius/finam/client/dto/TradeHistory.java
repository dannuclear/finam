package ru.nuclearius.finam.client.dto;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonGetter;

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
        private String tradeId;

        /** Символ инструмента */
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

        @JsonGetter("sum")
        public BigDecimal sum() {
            if (size == null || price == null)
                return null;
            return size.multiply(price).setScale(2, RoundingMode.HALF_UP);
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
