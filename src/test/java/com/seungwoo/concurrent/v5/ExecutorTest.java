package com.seungwoo.concurrent.v5;

import org.junit.jupiter.api.Test;

import java.util.concurrent.Executor;

import static org.assertj.core.api.Assertions.assertThat;

public class ExecutorTest {

    @Test
    void executorRun() {
        String[] holder = new String[1];
        final Runnable runnable = () -> holder[0] = Thread.currentThread().getName();

        // RunExecutor는 현재 스레드에서 직접 실행
        Executor executor = new RunExecutor();
        executor.execute(runnable);

        assertThat(holder[0]).isEqualTo(Thread.currentThread().getName());
    }

    static class RunExecutor implements Executor {

        @Override
        public void execute(Runnable command) {
            command.run();
        }
    }

    @Test
    void executorStart() throws InterruptedException {
        String[] holder = new String[1];
        final Runnable runnable = () -> holder[0] = Thread.currentThread().getName();

        // StartExecutor는 새로운 스레드에서 실행
        Executor executor = new StartExecutor();
        executor.execute(runnable);

        Thread.sleep(100);
        assertThat(holder[0]).isNotEqualTo(Thread.currentThread().getName());
    }

    static class StartExecutor implements Executor {

        @Override
        public void execute(final Runnable command) {
            new Thread(command).start();
        }
    }

}
