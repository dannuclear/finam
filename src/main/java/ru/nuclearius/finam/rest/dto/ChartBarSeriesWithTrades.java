package ru.nuclearius.finam.rest.dto;

import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import ru.nuclearius.finam.client.dto.TradeHistory;

@Getter
@Setter
@SuperBuilder
public class ChartBarSeriesWithTrades extends ChartBarSeries {
    private List<TradeHistory.Trade> trades;
}
