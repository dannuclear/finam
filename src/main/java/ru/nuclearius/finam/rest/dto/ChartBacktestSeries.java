package ru.nuclearius.finam.rest.dto;

import java.util.List;
import java.util.Map;

import org.springframework.data.geo.Point;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
public class ChartBacktestSeries extends ChartBarSeries {
    private List<ChartIndicatorSeries> indicators;
    private List<ChartSeriesMarker> trades;

    private Map<String, Object> statistics;
    private List<Point> normalDistribution;
}
