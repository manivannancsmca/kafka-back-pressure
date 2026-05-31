package com.kafka_back_pressure.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfiguration {

    @Bean
public NewTopic orderDlt(){

    return TopicBuilder
            .name("orders-topic-dlt")
            .partitions(4)
            .build();
}
}
