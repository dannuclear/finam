package ru.nuclearius.finam.rest.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TradeGroupDTO {
    private Integer id;
    private String name;
    private String description;
    private Boolean active;

    private List<TradeGroupTradedAssetDTO> tradedAssets;
    private List<TradeGroupReferenceAssetDTO> referenceAssets;
}