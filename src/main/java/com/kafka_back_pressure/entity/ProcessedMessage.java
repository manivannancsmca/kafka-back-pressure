package com.kafka_back_pressure.entity;

import java.io.Serializable;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.ToString;

@Entity
@Table(name="processed_messages")
@Data
@ToString
public class ProcessedMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    private String eventId;
}
