package com.seungwoo.concurrent.v8;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Slf4j
public class CompletableFutureMain {

    static void main() throws ExecutionException, InterruptedException {
        CompletableFuture<Void> future =
                CompletableFuture.supplyAsync(() -> "Thread: " + Thread.currentThread().getName())
                        .thenAccept(s -> log.info(s.toUpperCase()));

        future.get();
    }

}
