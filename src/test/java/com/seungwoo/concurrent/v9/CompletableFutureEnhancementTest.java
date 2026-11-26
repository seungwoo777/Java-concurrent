package com.seungwoo.concurrent.v9;

import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class CompletableFutureEnhancementTest {

    @Test
    void orTimeout() {
        // orTimeout (Java 9): 지정 시간 내 완료되지 않으면 TimeoutException
        CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            return "result";
        }).orTimeout(500, TimeUnit.MILLISECONDS);

        ExecutionException exception = assertThrows(ExecutionException.class, future::get);
        assertThat(exception.getCause()).isInstanceOf(TimeoutException.class);
    }

    @Test
    void completeOnTimeout() throws ExecutionException, InterruptedException {
        // completeOnTimeout (Java 9): 타임아웃 시 기본값으로 완료
        CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            return "result";
        }).completeOnTimeout("default", 500, TimeUnit.MILLISECONDS);

        String result = future.get();
        assertThat(result).isEqualTo("default");
    }

    @Test
    void copy() throws ExecutionException, InterruptedException {
        // copy (Java 9): 방어적 복사본 생성
        // 원본 future를 외부에 노출할 때 complete/cancel 방지
        CompletableFuture<String> original = CompletableFuture.completedFuture("hello");
        CompletableFuture<String> copy = original.copy();

        assertThat(copy.get()).isEqualTo("hello");

        // copy에 complete 호출해도 원본에 영향 없음
        copy.complete("modified");
        assertThat(original.get()).isEqualTo("hello");
    }

    @Test
    void failedFuture() {
        // failedFuture (Java 9): 이미 실패한 CompletableFuture 생성
        CompletableFuture<String> failed = CompletableFuture.failedFuture(
                new IllegalArgumentException("error"));

        assertThat(failed.isCompletedExceptionally()).isTrue();

        // exceptionally로 복구
        CompletableFuture<String> recovered = failed.exceptionally(Throwable::getMessage);
        assertThat(recovered.join()).contains("error");
    }

    @Test
    void completedStage() throws ExecutionException, InterruptedException {
        // completedStage (Java 9): CompletionStage만 반환 (complete 불가)
        // API 경계에서 불변 future를 반환할 때 유용
        CompletableFuture<String> future = CompletableFuture.completedStage("hello")
                .toCompletableFuture();

        assertThat(future.get()).isEqualTo("hello");
    }

}
