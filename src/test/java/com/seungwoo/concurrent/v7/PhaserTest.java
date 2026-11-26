package com.seungwoo.concurrent.v7;

import org.junit.jupiter.api.Test;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

public class PhaserTest {

    @Test
    void phaser_singlePhase() throws InterruptedException {
        // Phaser: CountDownLatch + CyclicBarrier 기능을 모두 제공
        // 동적으로 참여자 추가/제거 가능
        int parties = 3;
        Phaser phaser = new Phaser(parties);
        AtomicInteger completedCount = new AtomicInteger(0);
        CountDownLatch latch = new CountDownLatch(parties);

        for (int i = 0; i < parties; i++) {
            new Thread(() -> {
                // 모든 스레드가 도착할 때까지 대기
                phaser.arriveAndAwaitAdvance();
                completedCount.incrementAndGet();
                latch.countDown();
            }).start();
        }

        latch.await(5, TimeUnit.SECONDS);
        assertThat(completedCount.get()).isEqualTo(parties);
    }

    @Test
    void phaser_multiplePhases() throws InterruptedException {
        // 여러 phase를 순차적으로 진행
        int parties = 3;
        AtomicInteger phaseCount = new AtomicInteger(0);

        Phaser phaser = new Phaser(parties) {
            @Override
            protected boolean onAdvance(int phase, int registeredParties) {
                phaseCount.incrementAndGet();
                // phase 2가 끝나면 종료 (true 반환 시 Phaser 종료)
                return phase >= 2;
            }
        };

        CountDownLatch latch = new CountDownLatch(parties);

        for (int i = 0; i < parties; i++) {
            new Thread(() -> {
                // Phase 0
                phaser.arriveAndAwaitAdvance();
                // Phase 1
                phaser.arriveAndAwaitAdvance();
                // Phase 2
                phaser.arriveAndAwaitAdvance();

                latch.countDown();
            }).start();
        }

        latch.await(5, TimeUnit.SECONDS);

        // onAdvance가 3번 호출됨 (phase 0, 1, 2)
        assertThat(phaseCount.get()).isEqualTo(3);
        assertThat(phaser.isTerminated()).isTrue();
    }

    @Test
    void phaser_dynamicRegistration() {
        // 동적으로 참여자 추가/제거
        Phaser phaser = new Phaser(1); // 초기 1명

        assertThat(phaser.getRegisteredParties()).isEqualTo(1);

        phaser.register(); // 참여자 추가
        assertThat(phaser.getRegisteredParties()).isEqualTo(2);

        phaser.arriveAndDeregister(); // 도착하고 등록 해제
        assertThat(phaser.getRegisteredParties()).isEqualTo(1);
    }

}
