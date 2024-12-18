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

    /**
     * Test Case: 동일한 금액의 충전 요청을 여러번 보낸 후, 포인트 금액이 (충전 금액 * 요청 횟수)가 맞는지 확인합니다.
     * 작성 이유
     *  - 동일한 id로 여러 요청을 보내는 경우 한 번에 하나씩 처리되는지 확인합니다.
     * */
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

    /**
     * Test Case: 충전 및 사용 요청을 반복적으로 보내는 경우, 예외가 발생하지 않는지 확인합니다.
     * 작성 이유
     *  - 동일한 id로 여러 요청을 보내는 경우 순차적으로 처리되는지 확인합니다.
     *  - 동일 금액의 충전, 사용을 반복할 때 순서대로 처리되지 않는다면, 사용 시 충전 금액이 모자라 예외가 발생합니다.
     * */
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
