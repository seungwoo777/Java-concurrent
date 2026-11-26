package com.seungwoo.concurrent.v1;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RunnableMain {

    static void main() {
        Runnable runnable = () -> log.info("Runnable -> {}", Thread.currentThread().getName());

        Thread thread = new Thread(runnable);
        thread.start();
        log.info("Thread -> {}", thread.getName());
    }

}
