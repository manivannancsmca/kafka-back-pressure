package com.kafka_back_pressure.service;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.kafka_back_pressure.dto.OrderEvent;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderProducer {

    private final KafkaTemplate<String, OrderEvent> kafkaTemplate;

    public void publish(OrderEvent event) {

        kafkaTemplate.send(
                "orders-topic",
                event.orderId(),
                event
        );
    }
}
