package com.seungwoo.concurrent.util;

import org.junit.jupiter.api.Test;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

public class SemaphoreTest {

    @Test
    void semaphore_limitsAccess() throws InterruptedException {
        // Semaphore: 동시에 접근할 수 있는 스레드 수를 제한
        int permits = 3;
        Semaphore semaphore = new Semaphore(permits);
        AtomicInteger maxConcurrent = new AtomicInteger(0);
        AtomicInteger currentConcurrent = new AtomicInteger(0);

        int threadCount = 10;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            executorService.execute(() -> {
                try {
                    semaphore.acquire();
                    int current = currentConcurrent.incrementAndGet();
                    maxConcurrent.updateAndGet(max -> Math.max(max, current));

                    Thread.sleep(100); // 작업 시뮬레이션

                    currentConcurrent.decrementAndGet();
                    semaphore.release();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                latch.countDown();
            });
        }

        latch.await(10, TimeUnit.SECONDS);
        executorService.shutdown();

        // 동시에 최대 permits 수 만큼만 접근
        assertThat(maxConcurrent.get()).isLessThanOrEqualTo(permits);
    }

    @Test
    void tryAcquire() {
        Semaphore semaphore = new Semaphore(1);

        boolean first = semaphore.tryAcquire();
        assertThat(first).isTrue();

        // 이미 permit이 소진되었으므로 false
        boolean second = semaphore.tryAcquire();
        assertThat(second).isFalse();

        semaphore.release();

        boolean third = semaphore.tryAcquire();
        assertThat(third).isTrue();

        semaphore.release();
    }

}
