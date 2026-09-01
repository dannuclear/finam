package ru.nuclearius.finam.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.ta4j.core.BarSeries;
import org.ta4j.core.Trade.TradeType;
import org.ta4j.core.TradingRecord;
import org.ta4j.core.analysis.EquityCurveMode;
import org.ta4j.core.analysis.OpenPositionHandling;
import org.ta4j.core.criteria.NumberOfPositionsCriterion;
import org.ta4j.core.criteria.ReturnRepresentation;
import org.ta4j.core.criteria.VersusEnterAndHoldCriterion;
import org.ta4j.core.criteria.drawdown.MaximumDrawdownCriterion;
import org.ta4j.core.criteria.pnl.NetReturnCriterion;

import ru.nuclearius.finam.strategy.ChartStrategy.NamedAnalysisCriterion;

public class BacktestUtils {
    public static final List<NamedAnalysisCriterion> DEFAULT_CRITERIONS = List.of(
            new NamedAnalysisCriterion(
                    "Доходность стратегии",
                    new NetReturnCriterion(ReturnRepresentation.PERCENTAGE)),
            new NamedAnalysisCriterion(
                    "Максимальная просадка",
                    new MaximumDrawdownCriterion(EquityCurveMode.MARK_TO_MARKET,
                            OpenPositionHandling.MARK_TO_MARKET)),
            new NamedAnalysisCriterion(
                    "Количество открытых позиций",
                    new NumberOfPositionsCriterion()),
            new NamedAnalysisCriterion(
                    "Доля прибыльных сделок",
                    new VersusEnterAndHoldCriterion(
                            TradeType.BUY,
                            new NetReturnCriterion(ReturnRepresentation.PERCENTAGE),
                            BigDecimal.ONE,
                            ReturnRepresentation.PERCENTAGE)));

    public static Map<String, Object> calculateCriterions(BarSeries barSeries, TradingRecord tradingRecord) {
        return DEFAULT_CRITERIONS.stream()
                .collect(Collectors.toMap(
                        NamedAnalysisCriterion::getName,
                        c -> c.getAnalysisCriterion()
                                .calculate(barSeries, tradingRecord)
                                .bigDecimalValue().setScale(2, RoundingMode.HALF_UP)));
    }
}
