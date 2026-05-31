package com.kafka_back_pressure.service;

import java.util.concurrent.Semaphore;

import org.springframework.stereotype.Component;

@Component
public class BackPressureController {

    private final Semaphore semaphore =
            new Semaphore(100);

    public boolean acquire(){

        return semaphore.tryAcquire();
    }

    public void release(){

        semaphore.release();
    }
}
