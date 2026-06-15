package ru.nuclearius.finam.service.domain;

import java.util.List;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import ru.nuclearius.finam.client.dto.Bar;

public record BarsWithDescriptiveStatistics(
        List<Bar> bars,
        DescriptiveStatistics ds) {
}
