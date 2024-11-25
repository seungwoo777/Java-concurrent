package com.seungwoo.concurrent.javaV8;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public class CompletableFutureComposeTest {

    @Test
    void thenCompose() throws ExecutionException, InterruptedException {
        CompletableFuture<String> spring = CompletableFuture.supplyAsync(() -> {
            return "Spring";
        });

        // Future 간에 연관 관계가 있는 경우
        CompletableFuture<String> future = spring.thenCompose(this::boot);
        System.out.println(future.get());
    }

    private CompletableFuture<String> boot(String message) {
        return CompletableFuture.supplyAsync(() -> {
            return message + "-" + "Boot";
        });
    }

    @Test
    void thenCombine() throws ExecutionException, InterruptedException {
        CompletableFuture<String> spring = CompletableFuture.supplyAsync(() -> {
            return "Spring";
        });

        CompletableFuture<String> boot = CompletableFuture.supplyAsync(() -> {
            return "Boot";
        });

        CompletableFuture<String> future = spring.thenCombine(boot, (h, w) -> h + "-" + w);
        System.out.println(future.get());
    }

    @Test
    void allOf() throws ExecutionException, InterruptedException {
        CompletableFuture<String> spring = CompletableFuture.supplyAsync(() -> {
            return "Spring";
        });

        CompletableFuture<String> boot = CompletableFuture.supplyAsync(() -> {
            return "Boot";
        });

        List<CompletableFuture<String>> futures = List.of(spring, boot);

        CompletableFuture<List<String>> result = CompletableFuture.allOf(futures.toArray(new CompletableFuture[futures.size()]))
                .thenApply(v -> futures.stream().
                        map(CompletableFuture::join).
                        collect(Collectors.toList()));

        List<String> stringList = result.get();
        stringList.forEach(System.out::println);

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

        CompletableFuture<String> boot = CompletableFuture.supplyAsync(() -> {
            return "Boot";
        });

        CompletableFuture<Void> future = CompletableFuture.anyOf(spring, boot).thenAccept(System.out::println);
        future.get();
    }
}
