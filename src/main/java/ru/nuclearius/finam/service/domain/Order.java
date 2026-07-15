package ru.nuclearius.finam.service.domain;

import java.math.BigDecimal;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Order {
    private String accountId;
    private String symbol;
    private BigDecimal quantity;
    private Side side;
    private Type type;
    private TimeInForce timeInForce;
    private BigDecimal limitPrice;
    private BigDecimal stopPrice;
    private StopCondition stopCondition;
    private String clientOrderId;
    private ValidBefore validBefore;
    private String comment;

    public static enum Side {
        SIDE_UNSPECIFIED,
        SIDE_BUY,
        SIDE_SELL,
        UNRECOGNIZED
    }

    public static enum Type {
        ORDER_TYPE_UNSPECIFIED,
        ORDER_TYPE_MARKET,
        ORDER_TYPE_LIMIT,
        ORDER_TYPE_STOP,
        ORDER_TYPE_STOP_LIMIT,
        ORDER_TYPE_MULTI_LEG,
        UNRECOGNIZED
    }

    public static enum TimeInForce {
        TIME_IN_FORCE_UNSPECIFIED,
        TIME_IN_FORCE_DAY,
        TIME_IN_FORCE_GOOD_TILL_CANCEL,
        TIME_IN_FORCE_GOOD_TILL_CROSSING,
        TIME_IN_FORCE_EXT,
        TIME_IN_FORCE_ON_OPEN,
        TIME_IN_FORCE_ON_CLOSE,
        TIME_IN_FORCE_IOC,
        TIME_IN_FORCE_FOK,
        UNRECOGNIZED
    }

    public static enum StopCondition {
        STOP_CONDITION_UNSPECIFIED,
        STOP_CONDITION_LAST_UP,
        STOP_CONDITION_LAST_DOWN,
        UNRECOGNIZED
    }

    public enum ValidBefore {
        VALID_BEFORE_UNSPECIFIED,
        VALID_BEFORE_END_OF_DAY,
        VALID_BEFORE_GOOD_TILL_CANCEL,
        VALID_BEFORE_GOOD_TILL_DATE,
        UNRECOGNIZED
    }
}
