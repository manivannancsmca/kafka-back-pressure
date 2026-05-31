package com.kafka_back_pressure.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kafka_back_pressure.entity.OrderEntity;

public interface OrderRepository
        extends JpaRepository<OrderEntity, String> {
}
