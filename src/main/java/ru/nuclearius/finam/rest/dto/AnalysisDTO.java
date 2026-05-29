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
public class AnalysisDTO {
    private String name;

    private List<AnalysisAssetDTO> assets;
}