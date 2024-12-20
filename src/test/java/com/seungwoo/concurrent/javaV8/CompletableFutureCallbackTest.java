package com.seungwoo.concurrent.javaV8;

import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class CompletableFutureCallbackTest {

    @Test
    void thenApply() throws ExecutionException, InterruptedException {
        CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
           return "Thread -> " + Thread.currentThread().getName();
        }).thenApply(String::toUpperCase);

        System.out.println(future.get());
    }

    @Test
    void thenAccept() throws ExecutionException, InterruptedException {
        CompletableFuture<Void> future = CompletableFuture.supplyAsync(() -> {
            return "Thread -> " + Thread.currentThread().getName();
        }).thenAccept(s -> {
            System.out.println(s.toUpperCase());
        });

        future.get();
    }


    @Test
    void thenRun() throws ExecutionException, InterruptedException {
        CompletableFuture<Void> future = CompletableFuture.supplyAsync(() -> {
            return "Thread -> " + Thread.currentThread().getName();
        }).thenRun(() -> {
            System.out.println("Thread -> " + Thread.currentThread().getName());
        });

        future.get();
    }
}
