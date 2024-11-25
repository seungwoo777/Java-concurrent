package com.seungwoo.concurrent.javaV1;

import org.junit.jupiter.api.Test;

public class RunnableTest {

    @Test
    void runnable() {
        Runnable runnable = new Runnable() {

            @Override
            public void run() {
                System.out.println("runnable -> " + Thread.currentThread().getName());
            }
        };

        Thread thread = new Thread(runnable);
        thread.start();

        System.out.println("Thread -> " + Thread.currentThread().getName());
    }
}
