package com.seungwoo.concurrent.util;

import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

public class CountDownLatchTest {

    @Test
    void countDownLatch_basic() throws InterruptedException {
        // CountDownLatch: 지정된 횟수만큼 countDown()이 호출되면 await()가 풀림
        // 일회성 (재사용 불가)
        int taskCount = 3;
        CountDownLatch latch = new CountDownLatch(taskCount);
        ExecutorService executorService = Executors.newFixedThreadPool(taskCount);
        StringBuilder result = new StringBuilder();

        for (int i = 0; i < taskCount; i++) {
            final int taskId = i;
            executorService.execute(() -> {
                try {
                    Thread.sleep(100 * (taskId + 1));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                latch.countDown();
            });
        }

        // 모든 작업이 완료될 때까지 대기
        boolean completed = latch.await(5, TimeUnit.SECONDS);

        assertThat(completed).isTrue();
        assertThat(latch.getCount()).isEqualTo(0);
        executorService.shutdown();
    }

    @Test
    void countDownLatch_timeout() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        // countDown()이 호출되지 않으면 타임아웃
        boolean completed = latch.await(100, TimeUnit.MILLISECONDS);

        assertThat(completed).isFalse();
        assertThat(latch.getCount()).isEqualTo(1);
    }

}
