package com.seungwoo.concurrent.javaV1;

import org.junit.jupiter.api.Test;

public class ThreadTest {


    @Test
    void threadRun() {
        Thread thread = new Thread();

        thread.run();
        thread.run();
        thread.run();

        System.out.println("run -> " + Thread.currentThread().getName());
    }

    @Test
    void threadStart() {
        Thread thread = new MyThread();

        thread.start();

        System.out.println("start -> " + Thread.currentThread().getName());
    }

    static class MyThread extends Thread {
        @Override
        public void run() {
            System.out.println("myThread -> " + Thread.currentThread().getName());
        }
    }
}
