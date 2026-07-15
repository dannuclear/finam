package ru.nuclearius.finam.service.domain;

import java.math.BigDecimal;
import java.time.Instant;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderState {
    private String orderId;
    private String execId;
    private Status status;

    private Order order;

    private Instant transactAt;
    private Instant acceptAt;
    private Instant withdrawAt;

    private BigDecimal initialQuantity;
    private BigDecimal executedQuantity;
    private BigDecimal remainingQuantity;

    public static enum Status {
        ORDER_STATUS_UNSPECIFIED,
        ORDER_STATUS_NEW,
        ORDER_STATUS_PARTIALLY_FILLED,
        ORDER_STATUS_FILLED,
        ORDER_STATUS_DONE_FOR_DAY,
        ORDER_STATUS_CANCELED,
        ORDER_STATUS_REPLACED,
        ORDER_STATUS_PENDING_CANCEL,
        ORDER_STATUS_REJECTED,
        ORDER_STATUS_SUSPENDED,
        ORDER_STATUS_PENDING_NEW,
        ORDER_STATUS_EXPIRED,
        ORDER_STATUS_FAILED,
        ORDER_STATUS_FORWARDING,
        ORDER_STATUS_WAIT,
        ORDER_STATUS_DENIED_BY_BROKER,
        ORDER_STATUS_REJECTED_BY_EXCHANGE,
        ORDER_STATUS_WATCHING,
        ORDER_STATUS_EXECUTED,
        ORDER_STATUS_DISABLED,
        ORDER_STATUS_LINK_WAIT,
        ORDER_STATUS_SL_GUARD_TIME,
        ORDER_STATUS_SL_EXECUTED,
        ORDER_STATUS_SL_FORWARDING,
        ORDER_STATUS_TP_GUARD_TIME,
        ORDER_STATUS_TP_EXECUTED,
        ORDER_STATUS_TP_CORRECTION,
        ORDER_STATUS_TP_FORWARDING,
        ORDER_STATUS_TP_CORR_GUARD_TIME,
        UNRECOGNIZED
    }
}
