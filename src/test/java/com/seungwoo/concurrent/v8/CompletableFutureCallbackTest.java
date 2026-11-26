package com.seungwoo.concurrent.v8;

import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

public class CompletableFutureCallbackTest {

    @Test
    void thenApply() throws ExecutionException, InterruptedException {
        // thenApply: 결과를 변환하여 새로운 CompletableFuture 반환
        CompletableFuture<String> future =
                CompletableFuture.supplyAsync(() -> "hello")
                        .thenApply(String::toUpperCase);

        assertThat(future.get()).isEqualTo("HELLO");
    }

    @Test
    void thenAccept() throws ExecutionException, InterruptedException {
        // thenAccept: 결과를 소비 (반환값 없음)
        AtomicReference<String> holder = new AtomicReference<>();
        CompletableFuture<Void> future =
                CompletableFuture.supplyAsync(() -> "hello")
                        .thenAccept(s -> holder.set(s.toUpperCase()));

        future.get();
        assertThat(holder.get()).isEqualTo("HELLO");
    }

    @Test
    void thenRun() throws ExecutionException, InterruptedException {
        // thenRun: 결과를 무시하고 작업 실행
        AtomicReference<Boolean> executed = new AtomicReference<>(false);
        CompletableFuture<Void> future =
                CompletableFuture.supplyAsync(() -> "hello")
                        .thenRun(() -> executed.set(true));

        future.get();
        assertThat(executed.get()).isTrue();
    }

}
