package ru.nuclearius.finam.rest.dto;

import java.math.BigDecimal;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.nuclearius.finam.client.dto.Bar;
import ru.nuclearius.finam.db.Asset;
import ru.nuclearius.finam.db.TradeGroup;
import ru.nuclearius.finam.service.meta.ChangedEntity;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TradeGroupReferenceAssetDTO extends ChangedEntity {
    private Integer id;
    private TradeGroup tradeGroup;
    private Asset asset;
    private BigDecimal priceOffset;
    private String lineColor;
    private Short lineWidth;
    private Boolean enabled;
    private Integer panelNum;

    private List<Bar> bars;
    private List<Indicator> sma;
}
