package ru.nuclearius.finam.service.domain;

import java.math.BigDecimal;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AssetSetting {
    private String symbol;
    private BigDecimal priceOffset;
}
