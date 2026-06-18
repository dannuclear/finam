package ru.nuclearius.finam.rest.dto;

import java.util.List;

public record StrategyDto(
        String name,
        List<StrategyAssetDto> assets) {
}