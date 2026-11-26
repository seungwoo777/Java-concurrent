package com.seungwoo.concurrent.v8;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.assertThat;

public class CompletableFutureComposeTest {

    @Test
    void thenCompose() throws ExecutionException, InterruptedException {
        CompletableFuture<String> spring = CompletableFuture.supplyAsync(() -> "Spring");

        // thenCompose: Future 간에 연관 관계가 있는 경우 (flatMap과 유사)
        CompletableFuture<String> future = spring.thenCompose(this::boot);

        assertThat(future.get()).isEqualTo("Spring-Boot");
    }

    private CompletableFuture<String> boot(String message) {
        return CompletableFuture.supplyAsync(() -> message + "-" + "Boot");
    }

    @Test
    void thenCombine() throws ExecutionException, InterruptedException {
        CompletableFuture<String> spring = CompletableFuture.supplyAsync(() -> "Spring");
        CompletableFuture<String> boot = CompletableFuture.supplyAsync(() -> "Boot");

        // thenCombine: 두 개의 독립적인 Future 결과를 합침
        CompletableFuture<String> future = spring.thenCombine(boot, (h, w) -> h + "-" + w);

        assertThat(future.get()).isEqualTo("Spring-Boot");
    }

    @Test
    void allOf() throws ExecutionException, InterruptedException {
        CompletableFuture<String> spring = CompletableFuture.supplyAsync(() -> "Spring");
        CompletableFuture<String> boot = CompletableFuture.supplyAsync(() -> "Boot");

        List<CompletableFuture<String>> futures = List.of(spring, boot);

        // allOf: 모든 Future가 완료될 때까지 대기
        CompletableFuture<List<String>> result =
                CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                        .thenApply(v -> futures.stream()
                                .map(CompletableFuture::join)
                                .toList());

        assertThat(result.get()).containsExactly("Spring", "Boot");
    }

    @Test
    void anyOf() throws ExecutionException, InterruptedException {
        CompletableFuture<String> spring = CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(1000L);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            return "Spring";
        });
        CompletableFuture<String> boot = CompletableFuture.supplyAsync(() -> "Boot");

        // anyOf: 가장 먼저 완료된 Future의 결과를 반환
        Object result = CompletableFuture.anyOf(spring, boot).get();

        assertThat(result).isEqualTo("Boot");
    }

}
