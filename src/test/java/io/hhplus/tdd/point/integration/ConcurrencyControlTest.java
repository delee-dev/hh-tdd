package io.hhplus.tdd.point.integration;

import io.hhplus.tdd.point.dto.PointChargeRequest;
import io.hhplus.tdd.point.dto.PointUseRequest;
import io.hhplus.tdd.point.entity.UserPoint;
import io.hhplus.tdd.point.repository.UserPointRepository;
import io.hhplus.tdd.point.service.PointService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class ConcurrencyControlTest {
    @Autowired
    private PointService pointService;
    @Autowired
    private UserPointRepository userPointRepository;

    @Test
    void 동일한_사용자가_동시에_요청을_보내는_경우_한_번에_하나씩_처리된다() throws InterruptedException {
        // given
        long id = 1L;
        long pointToCharge = 10L;

        // when
        int threadCount = 100;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            executor.execute(() -> {
                try {
                    pointService.charge(id, new PointChargeRequest(pointToCharge));
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executor.shutdown();

        // then
        UserPoint userPoint = userPointRepository.selectById(id);
        assertThat(userPoint.point()).isEqualTo(pointToCharge * threadCount);
    }

    @Test
    void 동일한_사용자가_동시에_요청을_보내는_경우_순서대로_처리된다() throws InterruptedException {
        // given
        long id = 1L;
        long pointToCharge = 10L;
        long pointToUse = 10L;

        // when
        int threadCount = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount / 2; i++) {
            executor.execute(() -> {
                try {
                    pointService.charge(id, new PointChargeRequest(pointToCharge));
                } finally {
                    latch.countDown();
                }
            });

            executor.execute(() -> {
                try {
                    pointService.use(id, new PointUseRequest(pointToUse));
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executor.shutdown();

        // then
        UserPoint userPoint = userPointRepository.selectById(id);
        assertThat(userPoint.point()).isEqualTo(0);
    }
}
