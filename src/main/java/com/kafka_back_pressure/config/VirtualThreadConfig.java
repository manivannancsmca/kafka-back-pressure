package com.kafka_back_pressure.config;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class VirtualThreadConfig {

    @Bean
    ExecutorService executorService(){

        return Executors
                .newVirtualThreadPerTaskExecutor();
    }
}
