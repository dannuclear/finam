package ru.nuclearius.finam.client.dto;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class AssetInfo {

    private String board;
    private String id;
    private String ticker;
    private String mic;
    private String isin;
    private String type;
    private String name;

    /**
     * Количество десятичных знаков в цене
     */
    private Integer decimals;

    /**
     * Минимальный шаг цены (int64 в строке)
     * Финальный шаг = minStep / (10^decimals)
     */
    private Long minStep;

    /**
     * Размер лота
     */
    private BigDecimal lotSize;

    /**
     * Дата экспирации (может быть частичной)
     */
    private ExpirationDate expirationDate;

    /**
     * Валюта котировки
     */
    private String quoteCurrency;

    @Data
    public static class ExpirationDate {
        private Integer year;
        private Integer month;
        private Integer day;
    }
}
