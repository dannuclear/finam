package ru.nuclearius.finam.client.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

/**
 * Список транзакций
 */
@Getter
@Setter
public class TransactionList {

    /**
     * Транзакции по аккаунту
     */
    private List<Transaction> transactions = new ArrayList<>();

    @Getter
    @Setter
    public static class Transaction {

        /** Идентификатор транзакции */
        private String id;

        /** Тип транзакции */
        private TransactionCategory transactionCategory = TransactionCategory.OTHERS;

        /** Метка времени */
        private Instant timestamp;

        /** Символ инструмента */
        private String symbol;

        /** Изменение в денежном выражении */
        private TypeMoney change;

        /** Данные торговой транзакции (если применимо) */
        private TradeInfo trade;

        /** Тип транзакции (строковое поле из API, если используется отдельно) */
        private String category;

        /** Наименование транзакции */
        private String transactionName;

        /** Изменение количества */
        private BigDecimal changeQty;
    }

    /**
     * Торговая информация
     */
    @Getter
    @Setter
    public static class TradeInfo {

        private BigDecimal size;
        private BigDecimal price;
        private BigDecimal accruedInterest;
    }

    /**
     * Категория транзакции
     */
    public static enum TransactionCategory {
        OTHERS,
        DEPOSIT,
        WITHDRAW,
        INCOME,
        COMMISSION,
        TAX,
        INHERITANCE,
        TRANSFER,
        CONTRACT_TERMINATION,
        OUTCOMES,
        FINE,
        LOAN,
        UNRECOGNIZED
    }
}
