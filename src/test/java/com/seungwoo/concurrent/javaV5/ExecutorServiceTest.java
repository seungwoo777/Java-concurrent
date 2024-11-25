package com.seungwoo.concurrent.javaV5;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ExecutorServiceTest {

    @Test
    void shutdown() {
        ExecutorService executorService = Executors.newFixedThreadPool(100);

        Runnable runnable = () -> { System.out.println("Thread -> " + Thread.currentThread().getName()); };
        executorService.execute(runnable);

        executorService.shutdown();

        RejectedExecutionException rejectedExecutionException = assertThrows(RejectedExecutionException.class, () -> { executorService.execute(runnable); });
        assertThat(rejectedExecutionException).isInstanceOf(RejectedExecutionException.class);
    }

    @Test
    void shutdownNow() throws InterruptedException {
        Runnable runnable = () -> {
            System.out.println("Start");

            while (true) {
                if(Thread.currentThread().isInterrupted()) break;
            }
        };

        ExecutorService executorService = Executors.newFixedThreadPool(100);
        executorService.execute(runnable);

        executorService.shutdownNow();
        Thread.sleep(5000L);
    }

    @Test
    void invokeAll() throws InterruptedException, ExecutionException {
        ExecutorService executorService = Executors.newFixedThreadPool(100);
        Instant start = Instant.now();

        Callable<String> one = () -> {
            Thread.sleep(1000L);
            final String result = "One";
            System.out.println("result -> " + result);
            return result;
        };

        Callable<String> two = () -> {
            Thread.sleep(2000L);
            final String result = "Two";
            System.out.println("result -> " + result);
            return result;
        };

        Callable<String> three = () -> {
            Thread.sleep(3000L);
            final String result = "Three";
            System.out.println("result -> " + result);
            return result;
        };

        List<Future<String>> futures = executorService.invokeAll(Arrays.asList(one, two, three));

        for(Future<String> future : futures) {
            System.out.println(future.get());
        }

        System.out.println("time -> " + Duration.between(start, Instant.now()).getSeconds());
        executorService.shutdown();
    }

    @Test
    void invokeAny() throws InterruptedException, ExecutionException {
        ExecutorService executorService = Executors.newFixedThreadPool(100);
        Instant start = Instant.now();

        Callable<String> one = () -> {
            Thread.sleep(1000L);
            final String result = "One";
            System.out.println("result -> " + result);
            return result;
        };

        Callable<String> two = () -> {
            Thread.sleep(2000L);
            final String result = "Two";
            System.out.println("result -> " + result);
            return result;
        };

        Callable<String> three = () -> {
            Thread.sleep(3000L);
            final String result = "Three";
            System.out.println("result -> " + result);
            return result;
        };

        String result = executorService.invokeAny(Arrays.asList(one, two, three));
        System.out.println("result -> " + result + " time -> " + Duration.between(start, Instant.now()).getSeconds());

        executorService.shutdown();
    }
}
