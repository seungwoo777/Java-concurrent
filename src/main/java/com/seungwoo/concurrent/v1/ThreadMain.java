package com.seungwoo.concurrent.v1;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ThreadMain {

    static void main(String[] args) {
        start();
        run();
    }

    private static void run() {
        Thread thread = new Thread();
        thread.run();
        thread.run();
        thread.run();

        log.info("run -> {}", Thread.currentThread().getName());
    }

    private static void start() {
        Thread thread = new Thread();

        thread.start();

        log.info("start -> {}", Thread.currentThread().getName());
    }

    static class MyThread extends Thread {
        @Override
        public void run() {
            log.info("myThread -> {}", Thread.currentThread().getName());
        }
    }

}
