package com.seungwoo.concurrent.v8;

import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;

public class CompletableFutureRunTest {

    @Test
    void runAsync() throws ExecutionException, InterruptedException {
        // runAsync: 반환값 없는 비동기 작업 (기본 ForkJoinPool 사용)
        String[] holder = new String[1];
        CompletableFuture<Void> future = CompletableFuture.runAsync(
                () -> holder[0] = Thread.currentThread().getName()
        );

        future.get();
        assertThat(holder[0]).isNotEqualTo(Thread.currentThread().getName());
    }

    @Test
    void supplyAsync() throws ExecutionException, InterruptedException {
        // supplyAsync: 반환값 있는 비동기 작업
        CompletableFuture<String> future = CompletableFuture.supplyAsync(
                () -> "Thread -> " + Thread.currentThread().getName()
        );

        String result = future.get();
        assertThat(result).startsWith("Thread -> ");
        assertThat(result).doesNotContain(Thread.currentThread().getName());
    }

    @Test
    void supplyAsyncAnotherThreadPool() throws ExecutionException, InterruptedException {
        // 커스텀 ExecutorService를 지정하여 비동기 작업 실행
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        try {
            CompletableFuture<String> future = CompletableFuture.supplyAsync(
                    () -> "Thread -> " + Thread.currentThread().getName(), executorService
            );

            String result = future.get();
            assertThat(result).startsWith("Thread -> ");
        } finally {
            executorService.shutdown();
        }
    }

}
