package com.seungwoo.concurrent.v5;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.assertThat;

public class CallableTest {

    private ExecutorService executorService;

    @BeforeEach
    void setUp() {
        executorService = Executors.newSingleThreadExecutor();
    }

    @AfterEach
    void tearDown() {
        executorService.shutdown();
    }

    @Test
    void callable_void() throws ExecutionException, InterruptedException {
        Callable<Void> callable = () -> {
            System.out.println("Thread -> " + Thread.currentThread().getName());
            return null;
        };

        Future<Void> future = executorService.submit(callable);

        assertThat(future.get()).isNull();
    }

    @Test
    void callable_String() throws ExecutionException, InterruptedException {
        Callable<String> callable = () -> "Thread -> " + Thread.currentThread().getName();

        Future<String> future = executorService.submit(callable);

        assertThat(future.get()).startsWith("Thread -> ");
    }

}
