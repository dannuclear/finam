package ru.nuclearius.finam.client.dto;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class Option {

    private BigDecimal openInterest;
    private BigDecimal impliedVolatility;
    private BigDecimal theoreticalPrice;

    private BigDecimal delta;
    private BigDecimal gamma;
    private BigDecimal theta;
    private BigDecimal vega;
    private BigDecimal rho;

}