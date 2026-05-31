package com.kafka_back_pressure.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kafka_back_pressure.entity.ProcessedMessage;

public interface ProcessedMessageRepository
        extends JpaRepository<ProcessedMessage,String> {
}
