package com.seungwoo.concurrent.v5;

import org.junit.jupiter.api.Test;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static org.assertj.core.api.Assertions.assertThat;

public class ReadWriteLockTest {

    private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
    private String sharedData = "initial";

    @Test
    void multipleReadersAllowed() throws InterruptedException {
        // ReadLock은 여러 스레드가 동시에 획득 가능
        AtomicInteger maxConcurrentReaders = new AtomicInteger(0);
        AtomicInteger currentReaders = new AtomicInteger(0);
        int readerCount = 5;
        CountDownLatch latch = new CountDownLatch(readerCount);
        ExecutorService executorService = Executors.newFixedThreadPool(readerCount);

        for (int i = 0; i < readerCount; i++) {
            executorService.execute(() -> {
                readWriteLock.readLock().lock();
                try {
                    int current = currentReaders.incrementAndGet();
                    maxConcurrentReaders.updateAndGet(max -> Math.max(max, current));

                    Thread.sleep(200); // 읽기 작업 시뮬레이션
                    String data = sharedData;

                    currentReaders.decrementAndGet();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    readWriteLock.readLock().unlock();
                    latch.countDown();
                }
            });
        }

        latch.await(5, TimeUnit.SECONDS);
        executorService.shutdown();

        // 여러 reader가 동시에 읽기 가능
        assertThat(maxConcurrentReaders.get()).isGreaterThan(1);
    }

    @Test
    void writeLocksExclusive() throws InterruptedException {
        // WriteLock은 배타적 -> 다른 read/write 모두 차단
        CountDownLatch writeLatch = new CountDownLatch(1);
        CountDownLatch readAttemptLatch = new CountDownLatch(1);
        AtomicInteger readDuringWrite = new AtomicInteger(0);

        Thread writer = new Thread(() -> {
            readWriteLock.writeLock().lock();
            try {
                writeLatch.countDown(); // write lock 획득 완료 신호
                Thread.sleep(500);
                sharedData = "updated";
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                readWriteLock.writeLock().unlock();
            }
        });

        Thread reader = new Thread(() -> {
            try {
                writeLatch.await(); // writer가 lock 획득할 때까지 대기
                // tryLock으로 즉시 시도 -> write 중이므로 실패해야 함
                boolean acquired = readWriteLock.readLock().tryLock();
                if (acquired) {
                    readDuringWrite.incrementAndGet();
                    readWriteLock.readLock().unlock();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            readAttemptLatch.countDown();
        });

        writer.start();
        reader.start();

        readAttemptLatch.await(3, TimeUnit.SECONDS);
        writer.join(2000);

        // write 중에는 read가 불가능
        assertThat(readDuringWrite.get()).isEqualTo(0);
        assertThat(sharedData).isEqualTo("updated");
    }

    @Test
    void readWriteLock_cache() throws InterruptedException {
        // 실무 패턴: 캐시 구현 (read 많고 write 적은 경우에 적합)
        SimpleCache cache = new SimpleCache();
        int readerCount = 10;
        CountDownLatch latch = new CountDownLatch(readerCount + 1);
        ExecutorService executorService = Executors.newFixedThreadPool(readerCount + 1);

        // 1개의 writer
        executorService.execute(() -> {
            cache.put("key", "value");
            latch.countDown();
        });

        // writer가 먼저 실행되도록 약간 대기
        Thread.sleep(50);

        // 10개의 reader
        CopyOnWriteArrayList<String> results = new CopyOnWriteArrayList<>();
        for (int i = 0; i < readerCount; i++) {
            executorService.execute(() -> {
                String value = cache.get("key");
                if (value != null) results.add(value);
                latch.countDown();
            });
        }

        latch.await(5, TimeUnit.SECONDS);
        executorService.shutdown();

        assertThat(results).isNotEmpty();
        assertThat(results).allMatch(v -> v.equals("value"));
    }

    static class SimpleCache {
        private final ReadWriteLock lock = new ReentrantReadWriteLock();
        private final ConcurrentHashMap<String, String> map = new ConcurrentHashMap<>();

        public String get(String key) {
            lock.readLock().lock();
            try {
                return map.get(key);
            } finally {
                lock.readLock().unlock();
            }
        }

        public void put(String key, String value) {
            lock.writeLock().lock();
            try {
                map.put(key, value);
            } finally {
                lock.writeLock().unlock();
            }
        }
    }

}
