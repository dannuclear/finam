package ru.nuclearius.finam.rest.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class ChartSeries<T> {
    private String id;
    private String lineColor;
    private Short lineWidth;
    private String name;
    private Boolean enabled;
    private Integer panelNum;
    private List<T> values;
}