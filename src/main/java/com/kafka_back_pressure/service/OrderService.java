package com.kafka_back_pressure.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kafka_back_pressure.dto.OrderEvent;
import com.kafka_back_pressure.entity.OrderEntity;
import com.kafka_back_pressure.entity.ProcessedMessage;
import com.kafka_back_pressure.repository.OrderRepository;
import com.kafka_back_pressure.repository.ProcessedMessageRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {

    private final OrderRepository orderRepo;

    private final ProcessedMessageRepository processedRepo;

    public void process(OrderEvent event) {

        if (processedRepo.existsById(event.eventId())) {
            return;
        }

        OrderEntity order = new OrderEntity();

        order.setOrderId(event.orderId());
        order.setProduct(event.product());
        order.setQuantity(event.quantity());
        order.setStatus("CREATED");
        orderRepo.save(order);

        ProcessedMessage msg = new ProcessedMessage();
        msg.setEventId(event.eventId());
        processedRepo.save(msg);
    }
}