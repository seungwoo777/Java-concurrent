package com.seungwoo.concurrent.v8;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

import static org.assertj.core.api.Assertions.assertThat;

public class ParallelStreamTest {

    @Test
    void parallelStream_basic() {
        // parallelStream: ForkJoinPool.commonPool()을 사용하여 병렬 처리
        List<Integer> numbers = IntStream.rangeClosed(1, 100).boxed().toList();

        long sum = numbers.parallelStream()
                .mapToLong(Integer::longValue)
                .sum();

        assertThat(sum).isEqualTo(5050L);
    }

    @Test
    void parallelStream_usesMultipleThreads() {
        // 병렬 스트림은 여러 스레드에서 실행됨
        CopyOnWriteArrayList<String> threadNames = new CopyOnWriteArrayList<>();

        IntStream.rangeClosed(1, 1000)
                .parallel()
                .forEach(i -> {
                    String name = Thread.currentThread().getName();
                    if (!threadNames.contains(name)) {
                        threadNames.add(name);
                    }
                });

        // 여러 스레드가 사용됨 (main + ForkJoinPool worker들)
        assertThat(threadNames.size()).isGreaterThan(1);
    }

    @Test
    void parallelStream_customForkJoinPool() throws Exception {
        // 커스텀 ForkJoinPool을 사용하여 병렬 수준 제어
        // commonPool 대신 별도의 풀로 격리 가능
        ForkJoinPool customPool = new ForkJoinPool(2);

        try {
            long sum = customPool.submit(() ->
                    LongStream.rangeClosed(1, 100)
                            .parallel()
                            .sum()
            ).get();

            assertThat(sum).isEqualTo(5050L);
        } finally {
            customPool.shutdown();
        }
    }

    @Test
    void parallelStream_reduce() {
        // reduce: 병렬 스트림에서의 리듀스 연산
        // combiner가 중요 -> 병렬 처리 결과를 합침
        List<Integer> numbers = IntStream.rangeClosed(1, 10).boxed().toList();

        int sum = numbers.parallelStream()
                .reduce(0, Integer::sum, Integer::sum);

        assertThat(sum).isEqualTo(55);
    }

    @Test
    void sequential_vs_parallel_ordering() {
        // 순차 스트림: 순서 보장 / 병렬 스트림: forEachOrdered로 순서 보장 가능
        List<Integer> source = List.of(1, 2, 3, 4, 5);
        CopyOnWriteArrayList<Integer> orderedResult = new CopyOnWriteArrayList<>();

        source.parallelStream()
                .forEachOrdered(orderedResult::add);

        // forEachOrdered는 병렬이어도 원래 순서 보장
        assertThat(orderedResult).containsExactly(1, 2, 3, 4, 5);
    }

}
