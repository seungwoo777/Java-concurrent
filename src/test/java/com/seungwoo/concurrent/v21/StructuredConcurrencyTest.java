package com.seungwoo.concurrent.v21;

import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.StructuredTaskScope;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class StructuredConcurrencyTest {

    @Test
    void shutdownOnSuccess() throws InterruptedException, ExecutionException {
        // StructuredTaskScope.ShutdownOnSuccess: 하나가 성공하면 나머지 취소
        // invokeAny와 유사하지만 구조적
        try (var scope = new StructuredTaskScope.ShutdownOnSuccess<String>()) {
            scope.fork(() -> {
                Thread.sleep(1000);
                return "slow";
            });
            scope.fork(() -> "fast");

            scope.join();
            String result = scope.result();

            assertThat(result).isEqualTo("fast");
        }
    }

    @Test
    void shutdownOnFailure() throws InterruptedException {
        // StructuredTaskScope.ShutdownOnFailure: 하나가 실패하면 나머지 취소
        try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
            var user = scope.fork(() -> fetchUser());
            var order = scope.fork(() -> fetchOrder());

            scope.join();
            scope.throwIfFailed();

            assertThat(user.get()).isEqualTo("User-1");
            assertThat(order.get()).isEqualTo("Order-1");
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void shutdownOnFailure_propagatesException() {
        // 하위 작업이 실패하면 예외 전파
        assertThrows(ExecutionException.class, () -> {
            try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
                scope.fork(() -> {
                    throw new RuntimeException("Task failed");
                });
                scope.fork(() -> "success");

                scope.join();
                scope.throwIfFailed(); // 여기서 ExecutionException 발생
            }
        });
    }

    @Test
    void structuredConcurrency_parentChildLifecycle() throws InterruptedException, ExecutionException {
        // 구조적 동시성: 부모 scope가 닫히면 모든 자식 작업이 완료/취소됨
        // try-with-resources로 생명주기 관리 -> 스레드 누수 방지
        String result;
        try (var scope = new StructuredTaskScope.ShutdownOnSuccess<String>()) {
            scope.fork(() -> {
                Thread.sleep(100);
                return "completed";
            });

            scope.join();
            result = scope.result();
        }
        // scope 밖에서는 모든 하위 작업이 종료됨이 보장
        assertThat(result).isEqualTo("completed");
    }

    private String fetchUser() throws InterruptedException {
        Thread.sleep(100);
        return "User-1";
    }

    private String fetchOrder() throws InterruptedException {
        Thread.sleep(100);
        return "Order-1";
    }

}
