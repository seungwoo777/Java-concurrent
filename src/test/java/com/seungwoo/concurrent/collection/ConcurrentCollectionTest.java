package com.seungwoo.concurrent.collection;

import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

public class ConcurrentCollectionTest {

    @Test
    void concurrentHashMap_threadSafe() throws InterruptedException {
        // ConcurrentHashMap: 세분화된 lock으로 높은 동시성 제공
        ConcurrentHashMap<String, AtomicInteger> map = new ConcurrentHashMap<>();
        int threadCount = 100;
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            executorService.execute(() -> {
                map.computeIfAbsent("key", k -> new AtomicInteger(0)).incrementAndGet();
                latch.countDown();
            });
        }

        latch.await(5, TimeUnit.SECONDS);
        executorService.shutdown();

        assertThat(map.get("key").get()).isEqualTo(threadCount);
    }

    @Test
    void copyOnWriteArrayList_threadSafe() throws InterruptedException {
        // CopyOnWriteArrayList: 쓰기 시 배열을 복사
        // 읽기가 많고 쓰기가 적은 경우에 적합
        CopyOnWriteArrayList<Integer> list = new CopyOnWriteArrayList<>();
        int threadCount = 100;
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            final int value = i;
            executorService.execute(() -> {
                list.add(value);
                latch.countDown();
            });
        }

        latch.await(5, TimeUnit.SECONDS);
        executorService.shutdown();

        assertThat(list).hasSize(threadCount);
    }

    @Test
    void blockingQueue_producerConsumer() throws InterruptedException {
        // BlockingQueue: Producer-Consumer 패턴에 적합
        BlockingQueue<String> queue = new LinkedBlockingQueue<>(5);
        List<String> consumed = Collections.synchronizedList(new ArrayList<>());
        CountDownLatch latch = new CountDownLatch(3);

        // Producer
        Thread producer = new Thread(() -> {
            try {
                queue.put("A");
                queue.put("B");
                queue.put("C");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        // Consumer
        Thread consumer = new Thread(() -> {
            try {
                for (int i = 0; i < 3; i++) {
                    String item = queue.poll(2, TimeUnit.SECONDS);
                    if (item != null) {
                        consumed.add(item);
                        latch.countDown();
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        producer.start();
        consumer.start();

        latch.await(5, TimeUnit.SECONDS);
        producer.join(1000);
        consumer.join(1000);

        assertThat(consumed).containsExactly("A", "B", "C");
    }

}
