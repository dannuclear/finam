package ru.nuclearius.finam.client.dto;

import java.util.List;

import lombok.Data;

@Data
public class AllQuotes {
    private List<Quote> quotes;
}