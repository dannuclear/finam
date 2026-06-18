package ru.nuclearius.finam.rest.dto;

import java.util.List;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class BacktestResult {
    private List<Series> series;

    private BacktestStats stats;

    public static class BacktestStats {

    }
}
