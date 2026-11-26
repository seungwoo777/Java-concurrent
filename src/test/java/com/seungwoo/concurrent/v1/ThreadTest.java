package com.seungwoo.concurrent.v1;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ThreadTest {

    @Test
    void threadRun() {
        String[] holder = new String[1];
        Thread thread = new Thread(() -> holder[0] = Thread.currentThread().getName());

        // run()은 현재 스레드에서 동기적으로 실행
        thread.run();

        assertThat(holder[0]).isEqualTo(Thread.currentThread().getName());
    }

    @Test
    void threadStart() throws InterruptedException {
        String[] holder = new String[1];
        MyThread thread = new MyThread(holder);

        // start()는 새로운 스레드에서 비동기적으로 실행
        thread.start();
        thread.join();

        assertThat(holder[0]).isNotEqualTo(Thread.currentThread().getName());
    }

    static class MyThread extends Thread {
        private final String[] holder;

        MyThread(String[] holder) {
            this.holder = holder;
        }

        @Override
        public void run() {
            holder[0] = Thread.currentThread().getName();
        }
    }

}
