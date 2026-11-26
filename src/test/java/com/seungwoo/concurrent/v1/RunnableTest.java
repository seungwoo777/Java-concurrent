package com.seungwoo.concurrent.v1;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class RunnableTest {

    @Test
    void runnable() throws InterruptedException {
        Thread[] holder = new Thread[1];
        Runnable runnable = () -> holder[0] = Thread.currentThread();

        Thread thread = new Thread(runnable);
        thread.start();
        thread.join();

        // Runnable은 별도의 스레드에서 실행됨
        assertThat(holder[0]).isNotEqualTo(Thread.currentThread());
        assertThat(holder[0].getName()).startsWith("Thread-");
    }

}
