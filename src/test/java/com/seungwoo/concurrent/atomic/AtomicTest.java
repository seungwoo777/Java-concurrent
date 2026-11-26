package com.seungwoo.concurrent.atomic;

import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

public class AtomicTest {

    @Test
    void atomicInteger_threadSafe() throws InterruptedException {
        // AtomicInteger: CAS(Compare-And-Swap) 연산으로 lock 없이 thread-safe
        AtomicInteger atomicCount = new AtomicInteger(0);
        int threadCount = 1000;
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            executorService.execute(() -> {
                atomicCount.incrementAndGet();
                latch.countDown();
            });
        }

        latch.await(5, TimeUnit.SECONDS);
        executorService.shutdown();

        assertThat(atomicCount.get()).isEqualTo(threadCount);
    }

    @Test
    void atomicInteger_compareAndSet() {
        AtomicInteger value = new AtomicInteger(10);

        // CAS: 현재 값이 expected와 같을 때만 update
        boolean success = value.compareAndSet(10, 20);
        assertThat(success).isTrue();
        assertThat(value.get()).isEqualTo(20);

        // 현재 값이 10이 아니므로 실패
        boolean failed = value.compareAndSet(10, 30);
        assertThat(failed).isFalse();
        assertThat(value.get()).isEqualTo(20);
    }

    @Test
    void atomicInteger_getAndUpdate() {
        AtomicInteger value = new AtomicInteger(10);

        // getAndUpdate: 이전 값 반환 후 업데이트
        int previous = value.getAndUpdate(v -> v * 2);
        assertThat(previous).isEqualTo(10);
        assertThat(value.get()).isEqualTo(20);

        // updateAndGet: 업데이트 후 새 값 반환
        int updated = value.updateAndGet(v -> v + 5);
        assertThat(updated).isEqualTo(25);
    }

    @Test
    void atomicReference() {
        // AtomicReference: 객체 참조에 대한 원자적 연산
        AtomicReference<String> ref = new AtomicReference<>("Hello");

        boolean success = ref.compareAndSet("Hello", "World");
        assertThat(success).isTrue();
        assertThat(ref.get()).isEqualTo("World");
    }

}
