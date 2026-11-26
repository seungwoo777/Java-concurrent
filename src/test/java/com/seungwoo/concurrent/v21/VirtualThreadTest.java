package com.seungwoo.concurrent.v21;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

public class VirtualThreadTest {

    @Test
    void virtualThread_basic() throws InterruptedException {
        // Virtual Thread (Java 21): 경량 스레드
        // OS 스레드가 아닌 JVM이 관리하는 가상 스레드
        String[] holder = new String[1];

        Thread vThread = Thread.ofVirtual()
                .name("my-virtual-thread")
                .start(() -> holder[0] = Thread.currentThread().getName());

        vThread.join();

        assertThat(holder[0]).isEqualTo("my-virtual-thread");
        assertThat(vThread.isVirtual()).isTrue();
    }

    @Test
    void virtualThread_vs_platformThread() throws InterruptedException {
        // Platform Thread: OS 스레드와 1:1 매핑
        Thread platform = Thread.ofPlatform().start(() -> {});
        platform.join();
        assertThat(platform.isVirtual()).isFalse();

        // Virtual Thread: 경량, 많은 수를 생성 가능
        Thread virtual = Thread.ofVirtual().start(() -> {});
        virtual.join();
        assertThat(virtual.isVirtual()).isTrue();
    }

    @Test
    void virtualThread_massiveScale() throws InterruptedException {
        // Virtual Thread의 핵심 장점: 수만 개의 스레드를 가볍게 생성
        // Platform Thread는 이 정도를 만들면 OOM 발생 가능
        int threadCount = 10_000;
        AtomicInteger counter = new AtomicInteger(0);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            Thread.ofVirtual().start(() -> {
                counter.incrementAndGet();
                latch.countDown();
            });
        }

        boolean completed = latch.await(10, TimeUnit.SECONDS);
        assertThat(completed).isTrue();
        assertThat(counter.get()).isEqualTo(threadCount);
    }

    @Test
    void virtualThread_executorService() throws InterruptedException {
        // newVirtualThreadPerTaskExecutor: 작업마다 Virtual Thread 생성
        AtomicInteger counter = new AtomicInteger(0);
        int taskCount = 1000;
        CountDownLatch latch = new CountDownLatch(taskCount);

        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            for (int i = 0; i < taskCount; i++) {
                executor.submit(() -> {
                    counter.incrementAndGet();
                    latch.countDown();
                });
            }

            boolean completed = latch.await(10, TimeUnit.SECONDS);
            assertThat(completed).isTrue();
        }

        assertThat(counter.get()).isEqualTo(taskCount);
    }

    @Test
    void virtualThread_ioIntensive() throws InterruptedException {
        // Virtual Thread는 I/O 대기 시 carrier thread를 반환 (unmount)
        // -> I/O 집약적 작업에서 압도적 성능
        int taskCount = 1000;
        Instant start = Instant.now();
        CountDownLatch latch = new CountDownLatch(taskCount);

        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            for (int i = 0; i < taskCount; i++) {
                executor.submit(() -> {
                    try {
                        Thread.sleep(Duration.ofMillis(100)); // I/O 대기 시뮬레이션
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    latch.countDown();
                });
            }

            boolean completed = latch.await(10, TimeUnit.SECONDS);
            assertThat(completed).isTrue();
        }

        // 1000개 x 100ms지만 Virtual Thread 덕분에 10초보다 훨씬 빠르게 완료
        long elapsed = Duration.between(start, Instant.now()).toMillis();
        assertThat(elapsed).isLessThan(5000);
    }

    @Test
    void virtualThread_threadFactory() throws InterruptedException {
        // Thread.ofVirtual().factory(): Virtual Thread 팩토리
        ThreadFactory factory = Thread.ofVirtual()
                .name("worker-", 0)  // worker-0, worker-1, ...
                .factory();

        String[] names = new String[3];
        CountDownLatch latch = new CountDownLatch(3);

        for (int i = 0; i < 3; i++) {
            final int idx = i;
            Thread t = factory.newThread(() -> {
                names[idx] = Thread.currentThread().getName();
                latch.countDown();
            });
            t.start();
        }

        latch.await(5, TimeUnit.SECONDS);

        assertThat(names[0]).isEqualTo("worker-0");
        assertThat(names[1]).isEqualTo("worker-1");
        assertThat(names[2]).isEqualTo("worker-2");
    }

}
