package ru.nuclearius.finam.rest.dto;

import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.nuclearius.finam.client.dto.Bar;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Series {
    private String id;
    private String lineColor;
    private Short lineWidth;
    private String name;
    private Boolean enabled;
    private Integer panelNum;
    private List<Bar> bars;

    private Map<String, Object> extraParams;
}