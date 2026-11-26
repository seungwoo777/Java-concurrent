package com.seungwoo.concurrent.synchronization;

import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

public class SynchronizedTest {

    private int count = 0;
    private final Object lock = new Object();

    @Test
    void withoutSynchronized_raceCondition() throws InterruptedException {
        int threadCount = 1000;
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        CountDownLatch latch = new CountDownLatch(threadCount);

        // 동기화 없이 증가 -> race condition 발생 가능
        for (int i = 0; i < threadCount; i++) {
            executorService.execute(() -> {
                count++;
                latch.countDown();
            });
        }

        latch.await(5, TimeUnit.SECONDS);
        executorService.shutdown();

        // race condition으로 인해 1000보다 작을 수 있음
        System.out.println("without synchronized count: " + count);
    }

    @Test
    void withSynchronized_safe() throws InterruptedException {
        int threadCount = 1000;
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        CountDownLatch latch = new CountDownLatch(threadCount);

        // synchronized 블록으로 동기화 -> 안전한 증가
        for (int i = 0; i < threadCount; i++) {
            executorService.execute(() -> {
                synchronized (lock) {
                    count++;
                }
                latch.countDown();
            });
        }

        latch.await(5, TimeUnit.SECONDS);
        executorService.shutdown();

        assertThat(count).isEqualTo(threadCount);
    }

    @Test
    void synchronizedMethod() throws InterruptedException {
        Counter counter = new Counter();
        int threadCount = 1000;
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            executorService.execute(() -> {
                counter.increment();
                latch.countDown();
            });
        }

        latch.await(5, TimeUnit.SECONDS);
        executorService.shutdown();

        assertThat(counter.getCount()).isEqualTo(threadCount);
    }

    static class Counter {
        private int count = 0;

        public synchronized void increment() {
            count++;
        }

        public synchronized int getCount() {
            return count;
        }
    }

}
