package com.kafka_back_pressure.service;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

import com.kafka_back_pressure.dto.OrderEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderConsumer {

    private final OrderService service;
    private final BackPressureController bp;

    @KafkaListener(topics = "orders-topic")
    public void consume(OrderEvent event, Acknowledgment ack) {

        log.info("data>>>> ", event);
        if (!bp.acquire()) {
            log.warn("Back pressure detected");
            return;
        }

        try {
            service.process(event);
            ack.acknowledge();
        } finally {
            bp.release();
        }
    }
}
