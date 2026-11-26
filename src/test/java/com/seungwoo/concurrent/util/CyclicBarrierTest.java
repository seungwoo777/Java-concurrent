package com.seungwoo.concurrent.util;

import org.junit.jupiter.api.Test;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

public class CyclicBarrierTest {

    @Test
    void cyclicBarrier_basic() throws InterruptedException {
        // CyclicBarrier: 지정된 수의 스레드가 모두 도착해야 진행
        // CountDownLatch와 달리 재사용 가능 (Cyclic)
        int parties = 3;
        AtomicInteger arrivedCount = new AtomicInteger(0);
        CyclicBarrier barrier = new CyclicBarrier(parties, () -> {
            // 모든 스레드가 도착하면 실행되는 barrier action
            arrivedCount.set(parties);
        });

        ExecutorService executorService = Executors.newFixedThreadPool(parties);
        CountDownLatch latch = new CountDownLatch(parties);

        for (int i = 0; i < parties; i++) {
            executorService.execute(() -> {
                try {
                    barrier.await(5, TimeUnit.SECONDS);
                } catch (Exception e) {
                    Thread.currentThread().interrupt();
                }
                latch.countDown();
            });
        }

        latch.await(5, TimeUnit.SECONDS);
        assertThat(arrivedCount.get()).isEqualTo(parties);
        executorService.shutdown();
    }

    @Test
    void cyclicBarrier_reusable() throws InterruptedException {
        // CyclicBarrier는 재사용 가능
        int parties = 2;
        AtomicInteger barrierActionCount = new AtomicInteger(0);
        CyclicBarrier barrier = new CyclicBarrier(parties, barrierActionCount::incrementAndGet);

        ExecutorService executorService = Executors.newFixedThreadPool(parties);

        // 2라운드 실행
        for (int round = 0; round < 2; round++) {
            CountDownLatch latch = new CountDownLatch(parties);
            for (int i = 0; i < parties; i++) {
                executorService.execute(() -> {
                    try {
                        barrier.await(5, TimeUnit.SECONDS);
                    } catch (Exception e) {
                        Thread.currentThread().interrupt();
                    }
                    latch.countDown();
                });
            }
            latch.await(5, TimeUnit.SECONDS);
        }

        // barrier action이 2번 실행됨
        assertThat(barrierActionCount.get()).isEqualTo(2);
        executorService.shutdown();
    }

}
