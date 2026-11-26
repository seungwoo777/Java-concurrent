package com.seungwoo.concurrent.v7;

import org.junit.jupiter.api.Test;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.RecursiveTask;

import static org.assertj.core.api.Assertions.assertThat;

public class ForkJoinPoolTest {

    @Test
    void recursiveTask_sum() {
        // RecursiveTask: 반환값이 있는 분할 정복 작업
        // 1부터 100까지의 합을 병렬로 계산
        ForkJoinPool pool = new ForkJoinPool();

        SumTask task = new SumTask(1, 100);
        Long result = pool.invoke(task);

        // 1 + 2 + ... + 100 = 5050
        assertThat(result).isEqualTo(5050L);
        pool.shutdown();
    }

    @Test
    void recursiveAction_sort() {
        // RecursiveAction: 반환값이 없는 분할 정복 작업
        int[] array = {5, 3, 8, 1, 9, 2, 7, 4, 6, 0};
        ForkJoinPool pool = new ForkJoinPool();

        MergeSortAction action = new MergeSortAction(array, 0, array.length - 1);
        pool.invoke(action);

        assertThat(array).isSorted();
        pool.shutdown();
    }

    @Test
    void commonPool() {
        // ForkJoinPool.commonPool(): JVM 전체에서 공유하는 기본 풀
        // CompletableFuture, Parallel Stream이 기본으로 사용
        ForkJoinPool commonPool = ForkJoinPool.commonPool();

        assertThat(commonPool.getParallelism()).isGreaterThanOrEqualTo(1);

        SumTask task = new SumTask(1, 50);
        Long result = commonPool.invoke(task);

        assertThat(result).isEqualTo(1275L);
    }

    @Test
    void workStealing() {
        // Work-Stealing: 유휴 스레드가 다른 스레드의 작업 큐에서 작업을 훔쳐옴
        ForkJoinPool pool = new ForkJoinPool(4);

        SumTask task = new SumTask(1, 10000);
        Long result = pool.invoke(task);

        assertThat(result).isEqualTo(50005000L);
        assertThat(pool.getStealCount()).isGreaterThanOrEqualTo(0);
        pool.shutdown();
    }

    // 분할 정복으로 합계를 구하는 RecursiveTask
    static class SumTask extends RecursiveTask<Long> {
        private static final int THRESHOLD = 10;
        private final int start;
        private final int end;

        SumTask(int start, int end) {
            this.start = start;
            this.end = end;
        }

        @Override
        protected Long compute() {
            if (end - start <= THRESHOLD) {
                // 작업이 충분히 작으면 직접 계산
                long sum = 0;
                for (int i = start; i <= end; i++) {
                    sum += i;
                }
                return sum;
            }

            // 작업을 반으로 분할
            int mid = (start + end) / 2;
            SumTask left = new SumTask(start, mid);
            SumTask right = new SumTask(mid + 1, end);

            left.fork();  // 비동기로 실행
            Long rightResult = right.compute();  // 현재 스레드에서 실행
            Long leftResult = left.join();  // left 결과 대기

            return leftResult + rightResult;
        }
    }

    // 분할 정복으로 병합 정렬하는 RecursiveAction
    static class MergeSortAction extends RecursiveAction {
        private final int[] array;
        private final int left;
        private final int right;

        MergeSortAction(int[] array, int left, int right) {
            this.array = array;
            this.left = left;
            this.right = right;
        }

        @Override
        protected void compute() {
            if (left >= right) return;

            int mid = (left + right) / 2;
            MergeSortAction leftTask = new MergeSortAction(array, left, mid);
            MergeSortAction rightTask = new MergeSortAction(array, mid + 1, right);

            invokeAll(leftTask, rightTask);
            merge(left, mid, right);
        }

        private void merge(int left, int mid, int right) {
            int[] temp = new int[right - left + 1];
            int i = left, j = mid + 1, k = 0;

            while (i <= mid && j <= right) {
                temp[k++] = array[i] <= array[j] ? array[i++] : array[j++];
            }
            while (i <= mid) temp[k++] = array[i++];
            while (j <= right) temp[k++] = array[j++];

            System.arraycopy(temp, 0, array, left, temp.length);
        }
    }

}
