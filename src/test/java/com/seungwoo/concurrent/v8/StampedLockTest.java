package com.seungwoo.concurrent.v8;

import org.junit.jupiter.api.Test;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.StampedLock;

import static org.assertj.core.api.Assertions.assertThat;

public class StampedLockTest {

    private final StampedLock stampedLock = new StampedLock();
    private double x = 0, y = 0;

    @Test
    void writeLock() {
        // StampedLock writeLock: 배타적 쓰기 락
        long stamp = stampedLock.writeLock();
        try {
            x = 1.0;
            y = 2.0;
        } finally {
            stampedLock.unlockWrite(stamp);
        }

        assertThat(x).isEqualTo(1.0);
        assertThat(y).isEqualTo(2.0);
    }

    @Test
    void readLock() {
        // 먼저 값 설정
        long writeStamp = stampedLock.writeLock();
        x = 3.0;
        y = 4.0;
        stampedLock.unlockWrite(writeStamp);

        // StampedLock readLock: 비배타적 읽기 락
        long stamp = stampedLock.readLock();
        try {
            assertThat(x).isEqualTo(3.0);
            assertThat(y).isEqualTo(4.0);
        } finally {
            stampedLock.unlockRead(stamp);
        }
    }

    @Test
    void optimisticRead() {
        // Optimistic Read: 락을 걸지 않고 읽기 시도
        // ReadWriteLock보다 성능이 좋음 (읽기가 압도적으로 많은 경우)
        long writeStamp = stampedLock.writeLock();
        x = 5.0;
        y = 6.0;
        stampedLock.unlockWrite(writeStamp);

        // 낙관적 읽기: 락 없이 stamp만 획득
        long stamp = stampedLock.tryOptimisticRead();
        double currentX = x;
        double currentY = y;

        // validate: 읽는 동안 쓰기가 없었는지 확인
        if (stampedLock.validate(stamp)) {
            // 쓰기가 없었으면 읽은 값이 유효
            assertThat(currentX).isEqualTo(5.0);
            assertThat(currentY).isEqualTo(6.0);
        } else {
            // 쓰기가 있었으면 readLock으로 재시도
            stamp = stampedLock.readLock();
            try {
                currentX = x;
                currentY = y;
            } finally {
                stampedLock.unlockRead(stamp);
            }
        }
    }

    @Test
    void optimisticRead_invalidatedByWrite() throws InterruptedException {
        // 낙관적 읽기 중 쓰기가 발생하면 validate가 실패
        x = 1.0;
        y = 1.0;

        long stamp = stampedLock.tryOptimisticRead();
        double readX = x;
        double readY = y;

        // 다른 스레드에서 쓰기 수행
        Thread writer = new Thread(() -> {
            long ws = stampedLock.writeLock();
            try {
                x = 100.0;
                y = 200.0;
            } finally {
                stampedLock.unlockWrite(ws);
            }
        });
        writer.start();
        writer.join();

        // 쓰기가 발생했으므로 validate 실패
        assertThat(stampedLock.validate(stamp)).isFalse();
    }

    @Test
    void stampConversion_readToWrite() {
        // readLock을 writeLock으로 업그레이드
        x = 0;
        long stamp = stampedLock.readLock();
        try {
            if (x == 0) {
                // read -> write 변환 시도
                long writeStamp = stampedLock.tryConvertToWriteLock(stamp);
                if (writeStamp != 0L) {
                    stamp = writeStamp;
                    x = 10.0;
                } else {
                    // 변환 실패 시 readLock 해제 후 writeLock 획득
                    stampedLock.unlockRead(stamp);
                    stamp = stampedLock.writeLock();
                    x = 10.0;
                }
            }
        } finally {
            stampedLock.unlock(stamp);
        }

        assertThat(x).isEqualTo(10.0);
    }

}
