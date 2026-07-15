package ru.nuclearius.finam.rest.controller;

import java.util.Collection;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import ru.nuclearius.finam.service.OrderService;
import ru.nuclearius.finam.service.domain.OrderState;

@RestController
@RequestMapping("api/v1/orders")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;

    @GetMapping
    public Collection<OrderState> findAll() {
        return orderService.getAll();
    }
}
