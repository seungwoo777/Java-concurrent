package com.seungwoo.concurrent.v21;

import org.junit.jupiter.api.Test;

import java.util.concurrent.StructuredTaskScope;

import static org.assertj.core.api.Assertions.assertThat;

public class ScopedValueTest {

    // ScopedValue (Java 21): ThreadLocal 대체
    // - 불변 (한번 바인딩하면 변경 불가)
    // - 자식 스레드에 자동 상속 (Virtual Thread 친화적)
    // - 메모리 누수 없음 (scope 종료 시 자동 해제)
    private static final ScopedValue<String> CURRENT_USER = ScopedValue.newInstance();

    @Test
    void scopedValue_basic() {
        // ScopedValue.where().run(): 값을 바인딩하고 scope 내에서 사용
        ScopedValue.where(CURRENT_USER, "admin").run(() -> {
            assertThat(CURRENT_USER.get()).isEqualTo("admin");
        });

        // scope 밖에서는 바인딩 없음
        assertThat(CURRENT_USER.isBound()).isFalse();
    }

    @Test
    void scopedValue_call() throws Exception {
        // ScopedValue.where().call(): 반환값이 있는 경우
        String result = ScopedValue.where(CURRENT_USER, "admin").call(() -> {
            return "User is " + CURRENT_USER.get();
        });

        assertThat(result).isEqualTo("User is admin");
    }

    @Test
    void scopedValue_nested() {
        // 중첩 scope: 내부 scope가 외부 값을 덮어씀
        ScopedValue.where(CURRENT_USER, "outer").run(() -> {
            assertThat(CURRENT_USER.get()).isEqualTo("outer");

            ScopedValue.where(CURRENT_USER, "inner").run(() -> {
                assertThat(CURRENT_USER.get()).isEqualTo("inner");
            });

            // 내부 scope 종료 후 외부 값 복원
            assertThat(CURRENT_USER.get()).isEqualTo("outer");
        });
    }

    @Test
    void scopedValue_inheritedByVirtualThread() throws Exception {
        // ScopedValue는 StructuredTaskScope의 자식 스레드에 자동 상속
        ScopedValue.where(CURRENT_USER, "admin").run(() -> {
            try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
                var subtask = scope.fork(() -> {
                    // 자식 Virtual Thread에서도 부모의 ScopedValue 접근 가능
                    return CURRENT_USER.get();
                });

                scope.join();
                scope.throwIfFailed();

                assertThat(subtask.get()).isEqualTo("admin");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Test
    void scopedValue_vs_threadLocal() {
        // ThreadLocal 문제점:
        // 1. mutable -> 어디서든 set() 가능 (추적 어려움)
        // 2. remove() 안 하면 메모리 누수
        // 3. InheritableThreadLocal은 스레드 풀에서 위험

        // ScopedValue 장점:
        // 1. immutable -> scope 내에서 값 변경 불가
        // 2. scope 종료 시 자동 해제
        // 3. Virtual Thread에서 안전하게 상속

        ScopedValue.where(CURRENT_USER, "test").run(() -> {
            assertThat(CURRENT_USER.get()).isEqualTo("test");
            // CURRENT_USER.set("other") 같은 메서드 없음 -> 불변
        });
    }

}
