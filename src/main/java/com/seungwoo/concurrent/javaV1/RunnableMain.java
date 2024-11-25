package com.seungwoo.concurrent.javaV1;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RunnableMain {

    public static void main(String[] args) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                log.info("Runnable -> {}", Thread.currentThread().getName());
            }
        };

        Thread thread = new Thread(runnable);
        thread.start();
        log.info("Thread -> {}", thread.getName());
    }

}
