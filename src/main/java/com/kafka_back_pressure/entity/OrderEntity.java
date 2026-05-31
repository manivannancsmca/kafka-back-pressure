package com.kafka_back_pressure.entity;

import java.io.Serializable;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.ToString;

@Entity
@Table(name = "orders")
@Data
@ToString
public class OrderEntity implements Serializable {

    private static final long serialVersionUID = 1L; 

    @Id
    private String orderId;

    private String product;

    private Integer quantity;

    private String status;
}
