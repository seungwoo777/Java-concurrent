package com.seungwoo.concurrent.v5;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
public class ExecutorServiceMain {

    static void main() {
        ExecutorService executorService = Executors.newFixedThreadPool(100);
        Runnable runnable = () -> log.info("Thread -> {}", Thread.currentThread().getName());

        executorService.execute(runnable);
        executorService.shutdown();
    }

}
