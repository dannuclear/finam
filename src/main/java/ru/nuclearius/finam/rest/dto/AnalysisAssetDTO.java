package ru.nuclearius.finam.rest.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.nuclearius.finam.db.Asset;
import ru.nuclearius.finam.service.meta.ChangedEntity;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalysisAssetDTO extends ChangedEntity {
    private Integer id;
    private Asset asset;
    private String lineColor;
    private Short lineWidth;
    private Boolean enabled;
    private Integer panelIndex;
}
