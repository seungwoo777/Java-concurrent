package com.seungwoo.concurrent.v5;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ExecutorServiceTest {

    private ExecutorService executorService;

    @BeforeEach
    void setUp() {
        executorService = Executors.newFixedThreadPool(100);
    }

    @AfterEach
    void tearDown() {
        executorService.shutdown();
    }

    @Test
    void shutdown() {
        Runnable runnable = () -> System.out.println("Thread -> " + Thread.currentThread().getName());
        executorService.execute(runnable);
        executorService.shutdown();

        // shutdown 후 새로운 작업 제출 시 RejectedExecutionException 발생
        assertThrows(RejectedExecutionException.class,
                () -> executorService.execute(runnable));
    }

    @Test
    void shutdownNow() throws InterruptedException {
        Runnable runnable = () -> {
            while (!Thread.currentThread().isInterrupted()) {
                // busy wait
            }
        };

        executorService.execute(runnable);
        List<Runnable> notExecuted = executorService.shutdownNow();

        // shutdownNow 후 종료 대기
        boolean terminated = executorService.awaitTermination(2, TimeUnit.SECONDS);
        assertThat(terminated).isTrue();
    }

    @Test
    void invokeAll() throws InterruptedException, ExecutionException {
        Instant start = Instant.now();

        List<Future<String>> futures = executorService.invokeAll(createDelayedCallables());

        List<String> results = futures.stream()
                .map(f -> {
                    try { return f.get(); } catch (Exception e) { throw new RuntimeException(e); }
                })
                .toList();

        assertThat(results).containsExactly("One", "Two", "Three");

        // 병렬 실행이므로 가장 긴 작업(3초)에 맞춰 총 약 3초 소요
        long elapsed = Duration.between(start, Instant.now()).getSeconds();
        assertThat(elapsed).isLessThanOrEqualTo(4);
    }

    @Test
    void invokeAny() throws InterruptedException, ExecutionException {
        Instant start = Instant.now();

        // 가장 빨리 끝나는 작업의 결과를 반환
        String result = executorService.invokeAny(createDelayedCallables());

        assertThat(result).isEqualTo("One");

        long elapsed = Duration.between(start, Instant.now()).getSeconds();
        assertThat(elapsed).isLessThanOrEqualTo(2);
    }

    private List<Callable<String>> createDelayedCallables() {
        Callable<String> one = () -> {
            Thread.sleep(1000L);
            return "One";
        };
        Callable<String> two = () -> {
            Thread.sleep(2000L);
            return "Two";
        };
        Callable<String> three = () -> {
            Thread.sleep(3000L);
            return "Three";
        };
        return List.of(one, two, three);
    }

}
