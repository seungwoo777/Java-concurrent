package com.seungwoo.concurrent.v9;

import org.junit.jupiter.api.Test;

import java.util.concurrent.*;
import java.util.concurrent.Flow.*;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

public class FlowApiTest {

    @Test
    void submissionPublisher_basic() throws InterruptedException {
        // Flow API (Java 9): Reactive Streams 표준 구현
        // Publisher -> Subscriber로 비동기 데이터 스트림 전달
        CopyOnWriteArrayList<String> received = new CopyOnWriteArrayList<>();
        CountDownLatch latch = new CountDownLatch(3);

        SubmissionPublisher<String> publisher = new SubmissionPublisher<>();

        publisher.subscribe(new Subscriber<>() {
            private Subscription subscription;

            @Override
            public void onSubscribe(Subscription subscription) {
                this.subscription = subscription;
                subscription.request(Long.MAX_VALUE); // 모든 아이템 요청
            }

            @Override
            public void onNext(String item) {
                received.add(item);
                latch.countDown();
            }

            @Override
            public void onError(Throwable throwable) {
                throwable.printStackTrace();
            }

            @Override
            public void onComplete() {
                System.out.println("Completed");
            }
        });

        publisher.submit("A");
        publisher.submit("B");
        publisher.submit("C");
        publisher.close();

        latch.await(5, TimeUnit.SECONDS);

        assertThat(received).containsExactly("A", "B", "C");
    }

    @Test
    void submissionPublisher_withBackpressure() throws InterruptedException {
        // Backpressure: Subscriber가 처리 가능한 만큼만 요청
        CopyOnWriteArrayList<Integer> received = new CopyOnWriteArrayList<>();
        CountDownLatch latch = new CountDownLatch(3);

        SubmissionPublisher<Integer> publisher = new SubmissionPublisher<>();

        publisher.subscribe(new Subscriber<>() {
            private Subscription subscription;

            @Override
            public void onSubscribe(Subscription subscription) {
                this.subscription = subscription;
                subscription.request(1); // 1개만 먼저 요청
            }

            @Override
            public void onNext(Integer item) {
                received.add(item);
                latch.countDown();
                subscription.request(1); // 처리 완료 후 1개 더 요청
            }

            @Override
            public void onError(Throwable throwable) {}

            @Override
            public void onComplete() {}
        });

        publisher.submit(1);
        publisher.submit(2);
        publisher.submit(3);
        publisher.close();

        latch.await(5, TimeUnit.SECONDS);

        assertThat(received).containsExactly(1, 2, 3);
    }

    @Test
    void submissionPublisher_processor() throws InterruptedException {
        // Processor: Publisher이면서 Subscriber (데이터 변환 중간 단계)
        CopyOnWriteArrayList<String> received = new CopyOnWriteArrayList<>();
        CountDownLatch latch = new CountDownLatch(2);

        SubmissionPublisher<String> publisher = new SubmissionPublisher<>();

        // Processor: 소문자 -> 대문자 변환
        SubmissionPublisher<String> processor = new SubmissionPublisher<>();
        publisher.subscribe(new Subscriber<>() {
            private Subscription subscription;

            @Override
            public void onSubscribe(Subscription subscription) {
                this.subscription = subscription;
                subscription.request(Long.MAX_VALUE);
            }

            @Override
            public void onNext(String item) {
                processor.submit(item.toUpperCase()); // 변환 후 다음 단계로 전달
            }

            @Override
            public void onError(Throwable throwable) {}

            @Override
            public void onComplete() {
                processor.close();
            }
        });

        // 최종 Subscriber
        processor.subscribe(new Subscriber<>() {
            private Subscription subscription;

            @Override
            public void onSubscribe(Subscription subscription) {
                this.subscription = subscription;
                subscription.request(Long.MAX_VALUE);
            }

            @Override
            public void onNext(String item) {
                received.add(item);
                latch.countDown();
            }

            @Override
            public void onError(Throwable throwable) {}

            @Override
            public void onComplete() {}
        });

        publisher.submit("hello");
        publisher.submit("world");
        publisher.close();

        latch.await(5, TimeUnit.SECONDS);

        assertThat(received).containsExactly("HELLO", "WORLD");
    }

}
