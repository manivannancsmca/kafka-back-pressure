package com.kafka_back_pressure.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kafka_back_pressure.dto.OrderEvent;
import com.kafka_back_pressure.service.OrderProducer;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/orders")
public class OrderController {

    private final OrderProducer producer;

    @PostMapping
    public String createOrder(
            @RequestBody OrderEvent event){

        producer.publish(event);

        return "Accepted";
    }
}
