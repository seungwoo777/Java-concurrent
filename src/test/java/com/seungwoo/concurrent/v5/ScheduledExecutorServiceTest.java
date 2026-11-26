package com.seungwoo.concurrent.v5;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

public class ScheduledExecutorServiceTest {

    private ScheduledExecutorService executorService;

    @BeforeEach
    void setUp() {
        executorService = Executors.newSingleThreadScheduledExecutor();
    }

    @AfterEach
    void tearDown() {
        executorService.shutdown();
    }

    @Test
    void schedule() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        Runnable runnable = latch::countDown;

        // 1초 후 실행
        executorService.schedule(runnable, 1, TimeUnit.SECONDS);

        boolean completed = latch.await(3, TimeUnit.SECONDS);
        assertThat(completed).isTrue();
    }

    @Test
    void scheduleAtFixedRate() throws InterruptedException {
        AtomicInteger count = new AtomicInteger(0);
        CountDownLatch latch = new CountDownLatch(3);

        // scheduleAtFixedRate: 작업 시작 시점 기준으로 주기 계산
        // 작업이 500ms 걸려도, 1초 간격으로 시작
        Runnable runnable = () -> {
            count.incrementAndGet();
            latch.countDown();
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        };

        executorService.scheduleAtFixedRate(runnable, 0, 1, TimeUnit.SECONDS);

        boolean completed = latch.await(5, TimeUnit.SECONDS);
        assertThat(completed).isTrue();
        assertThat(count.get()).isGreaterThanOrEqualTo(3);
    }

    @Test
    void scheduleWithFixedDelay() throws InterruptedException {
        AtomicInteger count = new AtomicInteger(0);
        CountDownLatch latch = new CountDownLatch(3);

        // scheduleWithFixedDelay: 작업 종료 시점 기준으로 delay 후 다음 실행
        // 작업이 500ms 걸리면, 종료 후 1초 뒤에 다음 작업 시작 (총 1.5초 간격)
        Runnable runnable = () -> {
            count.incrementAndGet();
            latch.countDown();
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        };

        executorService.scheduleWithFixedDelay(runnable, 0, 1, TimeUnit.SECONDS);

        boolean completed = latch.await(6, TimeUnit.SECONDS);
        assertThat(completed).isTrue();
        assertThat(count.get()).isGreaterThanOrEqualTo(3);
    }

}
