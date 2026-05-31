package com.kafka_back_pressure.dto;

public record OrderEvent(

        String eventId,

        String orderId,

        String product,

        Integer quantity

) {}
