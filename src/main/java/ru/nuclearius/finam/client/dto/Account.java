package ru.nuclearius.finam.client.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Информация о конкретном аккаунте.
 */
@Getter
@Setter
public class Account {

    /** Идентификатор аккаунта */
    private String accountId;

    /** Тип аккаунта */
    private String type;

    /** Статус аккаунта */
    private String status;

    /** Текущий капитал */
    private BigDecimal equity;

    /** Нереализованная прибыль */
    private BigDecimal unrealizedProfit;

    /** Открытые и теоретические позиции */
    private List<Position> positions = new ArrayList<>();

    /**
     * Денежные средства на счете,
     * доступные для торговли (без маржи)
     */
    private List<Money> cash = new ArrayList<>();

    /**
     * Портфель Московской биржи (единый или моно-счет)
     */
    private PortfolioMC portfolioMc;

    /**
     * Портфель срочного рынка (FORTS)
     */
    private PortfolioForts portfolioForts;

    /** Дата открытия счета */
    private Instant openAccountDate;

    /** Дата первой торговой транзакции */
    private Instant firstTradeDate;

    /** Дата первой неторговой транзакции */
    private Instant firstNonTradeDate;

    // =========================================================
    // ====================== INNER TYPES ======================
    // =========================================================

    /**
     * Информация о позиции.
     */
    @Getter
    @Setter
    public static class Position {

        /** Символ инструмента */
        private String symbol;

        /** Количество инструмента */
        private BigDecimal quantity;

        /** Средняя цена покупки */
        private BigDecimal averagePrice;

        /** Текущая цена */
        private BigDecimal currentPrice;

        /** Поддерживающая маржа */
        private BigDecimal maintenanceMargin;

        /** Дневной PnL */
        private BigDecimal dailyPnl;

        /** Нереализованный PnL */
        private BigDecimal unrealizedPnl;
    }

    /**
     * Представление денежных средств.
     */
    @Getter
    @Setter
    public static class Money {
        private String currencyCode;
        private Long units;
        private Integer nanos;
    }

    /**
     * Портфель Московской биржи (ЕТС или моно-счет).
     */
    @Getter
    @Setter
    public static class PortfolioMC {

        /** Доступные денежные средства */
        private BigDecimal availableCash;

        /** Начальная маржа */
        private BigDecimal initialMargin;

        /** Поддерживающая маржа */
        private BigDecimal maintenanceMargin;
    }

    /**
     * Портфель срочного рынка (FORTS).
     */
    @Getter
    @Setter
    public static class PortfolioForts {

        /** Доступные денежные средства */
        private BigDecimal availableCash;

        /** Зарезервированные средства */
        private BigDecimal moneyReserved;
    }
}
