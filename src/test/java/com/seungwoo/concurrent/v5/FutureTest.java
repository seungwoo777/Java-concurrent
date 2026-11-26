package com.seungwoo.concurrent.v5;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class FutureTest {

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
    void get() throws ExecutionException, InterruptedException {
        Future<String> future = executorService.submit(callable());

        // It takes 3 seconds by blocking(블로킹에 의해 3초 걸림)
        String result = future.get();

        assertThat(result).startsWith("Thread: ");
    }

    @Test
    void get_timeout() {
        Future<String> future = executorService.submit(callable());

        // 3초 걸리는 작업에 1초 타임아웃 -> TimeoutException 발생
        assertThrows(TimeoutException.class,
                () -> future.get(1, TimeUnit.SECONDS));
    }

    @Test
    void isCancelled_False() {
        Future<String> future = executorService.submit(callable());

        assertThat(future.isCancelled()).isFalse();
    }

    @Test
    void isCancelled_True() {
        Future<String> future = executorService.submit(callable());
        future.cancel(true);

        assertThat(future.isCancelled()).isTrue();
    }

    @Test
    void isDone_False() {
        Future<String> future = executorService.submit(callable());

        assertThat(future.isDone()).isFalse();
    }

    @Test
    void isDone_True() throws ExecutionException, InterruptedException {
        Future<String> future = executorService.submit(callable());

        // 작업이 끝날 때까지 대기
        future.get();

        assertThat(future.isDone()).isTrue();
    }

    @Test
    void cancel_then_get_throws_CancellationException() {
        Future<String> future = executorService.submit(callable());
        future.cancel(true);

        assertThrows(CancellationException.class, future::get);
    }

    private Callable<String> callable() {
        return () -> {
            Thread.sleep(3000L);
            return "Thread: " + Thread.currentThread().getName();
        };
    }

}
