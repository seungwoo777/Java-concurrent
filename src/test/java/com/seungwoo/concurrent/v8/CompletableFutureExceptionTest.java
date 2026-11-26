package com.seungwoo.concurrent.v8;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.assertThat;

public class CompletableFutureExceptionTest {

    @ParameterizedTest(name = "exceptionally - doThrow={0}")
    @ValueSource(booleans = {true, false})
    void exceptionally(boolean doThrow) throws ExecutionException, InterruptedException {
        CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
            if (doThrow) {
                throw new IllegalArgumentException("Invalid Argument");
            }
            return "Success";
        }).exceptionally(Throwable::getMessage);

        String result = future.get();
        if (doThrow) {
            assertThat(result).contains("Invalid Argument");
        } else {
            assertThat(result).isEqualTo("Success");
        }
    }

    @ParameterizedTest(name = "handle - doThrow={0}")
    @ValueSource(booleans = {true, false})
    void handle(boolean doThrow) throws ExecutionException, InterruptedException {
        CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
            if (doThrow) {
                throw new IllegalArgumentException("Invalid Argument");
            }
            return "Success";
        }).handle((result, e) -> e == null ? result : e.getMessage());

        String result = future.get();
        if (doThrow) {
            assertThat(result).contains("Invalid Argument");
        } else {
            assertThat(result).isEqualTo("Success");
        }
    }

}
