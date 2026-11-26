package com.seungwoo.concurrent.util;

import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

public class ThreadLocalTest {

    private final ThreadLocal<String> threadLocal = new ThreadLocal<>();

    @Test
    void threadLocal_eachThreadHasOwnValue() throws InterruptedException {
        // ThreadLocal: 각 스레드마다 독립적인 변수 저장소
        AtomicReference<String> thread1Value = new AtomicReference<>();
        AtomicReference<String> thread2Value = new AtomicReference<>();
        CountDownLatch latch = new CountDownLatch(2);

        Thread t1 = new Thread(() -> {
            threadLocal.set("Thread-1-Value");
            try { Thread.sleep(100); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            thread1Value.set(threadLocal.get());
            threadLocal.remove(); // 메모리 누수 방지
            latch.countDown();
        });

        Thread t2 = new Thread(() -> {
            threadLocal.set("Thread-2-Value");
            try { Thread.sleep(100); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            thread2Value.set(threadLocal.get());
            threadLocal.remove();
            latch.countDown();
        });

        t1.start();
        t2.start();
        latch.await(5, TimeUnit.SECONDS);

        // 각 스레드가 자신만의 값을 가짐
        assertThat(thread1Value.get()).isEqualTo("Thread-1-Value");
        assertThat(thread2Value.get()).isEqualTo("Thread-2-Value");
    }

    @Test
    void threadLocal_withInitial() throws InterruptedException {
        // withInitial: 초기값을 Supplier로 지정
        ThreadLocal<Integer> counter = ThreadLocal.withInitial(() -> 0);
        AtomicReference<Integer> result = new AtomicReference<>();

        Thread thread = new Thread(() -> {
            assertThat(counter.get()).isEqualTo(0); // 초기값
            counter.set(42);
            result.set(counter.get());
            counter.remove();
        });

        thread.start();
        thread.join(2000);

        assertThat(result.get()).isEqualTo(42);

        // 메인 스레드에서는 여전히 초기값
        assertThat(counter.get()).isEqualTo(0);
        counter.remove();
    }

    @Test
    void inheritableThreadLocal() throws InterruptedException {
        // InheritableThreadLocal: 부모 스레드의 값을 자식 스레드가 상속
        InheritableThreadLocal<String> inheritable = new InheritableThreadLocal<>();
        inheritable.set("ParentValue");

        AtomicReference<String> childValue = new AtomicReference<>();

        Thread child = new Thread(() -> {
            childValue.set(inheritable.get());
            inheritable.remove();
        });

        child.start();
        child.join(2000);

        assertThat(childValue.get()).isEqualTo("ParentValue");
        inheritable.remove();
    }

}
