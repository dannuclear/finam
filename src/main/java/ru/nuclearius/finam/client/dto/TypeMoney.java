package ru.nuclearius.finam.client.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TypeMoney {

  private String currencyCode;

  private String units;

  private Integer nanos;
}
