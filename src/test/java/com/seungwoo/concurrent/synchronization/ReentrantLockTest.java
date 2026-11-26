package com.seungwoo.concurrent.synchronization;

import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import static org.assertj.core.api.Assertions.assertThat;

public class ReentrantLockTest {

    private final ReentrantLock lock = new ReentrantLock();
    private int count = 0;

    @Test
    void reentrantLock_basic() throws InterruptedException {
        int threadCount = 1000;
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            executorService.execute(() -> {
                lock.lock();
                try {
                    count++;
                } finally {
                    lock.unlock();
                }
                latch.countDown();
            });
        }

        latch.await(5, TimeUnit.SECONDS);
        executorService.shutdown();

        assertThat(count).isEqualTo(threadCount);
    }

    @Test
    void tryLock() throws InterruptedException {
        // tryLock: 락 획득을 시도하고, 실패하면 즉시 false 반환 (블로킹하지 않음)
        lock.lock();

        Thread thread = new Thread(() -> {
            boolean acquired = lock.tryLock();
            assertThat(acquired).isFalse(); // 이미 잠겨 있으므로 획득 실패
        });

        thread.start();
        thread.join();
        lock.unlock();
    }

    @Test
    void tryLock_withTimeout() throws InterruptedException {
        lock.lock();

        Thread thread = new Thread(() -> {
            try {
                // 최대 100ms 동안 락 획득 시도
                boolean acquired = lock.tryLock(100, TimeUnit.MILLISECONDS);
                assertThat(acquired).isFalse();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        thread.start();
        thread.join();
        lock.unlock();
    }

    @Test
    void reentrant() {
        // ReentrantLock은 같은 스레드에서 여러 번 lock 가능 (재진입)
        lock.lock();
        lock.lock(); // 같은 스레드이므로 성공

        assertThat(lock.getHoldCount()).isEqualTo(2);

        lock.unlock();
        lock.unlock();

        assertThat(lock.getHoldCount()).isEqualTo(0);
    }

}
