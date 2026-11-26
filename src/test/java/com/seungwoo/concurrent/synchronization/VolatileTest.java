package com.seungwoo.concurrent.synchronization;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class VolatileTest {

    private volatile boolean running = true;

    @Test
    void volatile_visibility() throws InterruptedException {
        // volatile: 변수의 값을 CPU 캐시가 아닌 메인 메모리에서 읽고 씀
        // 한 스레드의 변경이 다른 스레드에 즉시 보임 (가시성 보장)
        Thread worker = new Thread(() -> {
            while (running) {
                // volatile이 없으면 이 루프가 영원히 돌 수 있음 (CPU 캐시에서 읽으므로)
            }
        });

        worker.start();

        Thread.sleep(100);
        running = false; // volatile이므로 worker 스레드에 즉시 반영

        worker.join(2000);
        assertThat(worker.isAlive()).isFalse();
    }

}
