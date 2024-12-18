package io.hhplus.tdd.point.service;

import io.hhplus.tdd.point.entity.PointHistory;
import io.hhplus.tdd.point.entity.type.TransactionType;
import io.hhplus.tdd.point.entity.UserPoint;
import io.hhplus.tdd.point.dto.PointChargeRequest;
import io.hhplus.tdd.point.dto.PointUseRequest;
import io.hhplus.tdd.point.repository.PointHistoryRepository;
import io.hhplus.tdd.point.repository.UserPointRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PointServiceTest {
    @InjectMocks
    private PointService pointService;
    @Mock
    private UserPointRepository userPointRepository;
    @Mock
    private PointHistoryRepository pointHistoryRepository;
    @Mock
    private PointValidator pointValidator;

    @Nested
    @DisplayName("포인트 조회")
    class ReadUserPointTest {
        /**
         * Test Case: 사용자가 시스템에 포인트를 등록(충전)한 경우, 해당 UserPoint 객체를 반환해야 합니다.
         * 작성 이유
         *  - 반환된 UserPoint 객체의 값들이 정확히 일치하는지 확인합니다.
         * */
        @Test
        void 포인트가_충전된_유저는_해당하는_UserPoint를_반환한다() {
            // given
            long id = 1L;
            long point = 100L;
            long updateMillis = System.currentTimeMillis();
            UserPoint expected = new UserPoint(id, point, updateMillis);

            when(userPointRepository.selectById(id))
                    .thenReturn(expected);

            // when
            UserPoint actual = pointService.getPoint(id);

            //then
            assertThat(actual)
                    .usingRecursiveAssertion()
                    .isEqualTo(expected);
        }

        /**
         * Test Case: 사용자가 시스템에 포인트를 등록(충전)하지 않은 경우, 포인트가 0인 UserPoint 객체(기본 값)를 반환해야 합니다.
         * 작성 이유
         *  - 등록(충전)하지 않은 사용자에 대해 기본 값을 반환하는지 검증합니다.
         * */
        @Test
        void 포인트가_충전되지_않은_유저는_기본_값을_반환한다() {
            // given
            long id = 1L;
            UserPoint expected = UserPoint.empty(id);

            when(userPointRepository.selectById(id))
                    .thenReturn(expected);

            // when
            UserPoint actual = pointService.getPoint(id);

            // then
            assertThat(actual.id()).isEqualTo(expected.id());
            assertThat(actual.point()).isEqualTo(expected.point());
        }
    }

    @Nested
    @DisplayName("포인트 내역 조회")
    class ReadPointHistoriesTest {
        /**
         * Test Case: 사용자가 포인트를 충전 및 사용한 내역이 있다면, 해당 내역을 반환해야 합니다.
         * 작성 이유
         *  - 반환된 포인트 내역이 정확히 일치하는지 확인합니다.
         * */
        @Test
        void 포인트_내역이_있는_경우_해당_내역을_반환한다() {
            // given
            long userId = 1L;

            List<PointHistory> expected = new ArrayList<>();
            PointHistory chargingHistory = new PointHistory(userId, 1L, 200, TransactionType.CHARGE, System.currentTimeMillis());
            PointHistory usingHistory = new PointHistory(userId, 2L, 100, TransactionType.USE, System.currentTimeMillis());
            expected.add(chargingHistory);
            expected.add(usingHistory);

            when(pointHistoryRepository.selectAllByUserId(userId))
                    .thenReturn(expected);

            // when
            List<PointHistory> actual = pointService.getHistories(userId);

            //then
            assertThat(actual)
                    .usingRecursiveComparison()
                    .isEqualTo(expected);
        }

        /**
         * Test Case: 사용자가 시스템에 포인트를 충전 및 사용한 내역이 없다면, 빈 리스트를 반환해야 합니다.
         * 작성 이유
         *  - 포인트 내역이 없는 경우, 빈 리스트를 반환하는지 확인합니다.
         * */
        @Test
        void 포인트_내역이_없는_경우_빈_리스트를_반환한다() {
            // given
            long userId = 1L;
            List<PointHistory> expected = Collections.emptyList();

            when(pointHistoryRepository.selectAllByUserId(userId))
                    .thenReturn(expected);

            // when
            List<PointHistory> actual = pointService.getHistories(userId);

            // then
            assertThat(actual)
                    .isEmpty();
        }
    }

    @Nested
    @DisplayName("포인트 충전")
    class ChargePointTest {
        /**
         * Test Case: 충전 후 금액은 (충전 전 금액 + 충전 금액)과 일치해야 합니다.
         * 작성 이유
         *  - 포인트 충전 기능이 정확하게 동작하는지 검증합니다.
         * */
        @Test
        void 충전_후_잔고는_충전_전_잔고에_충전_금액을_더한_값과_일치한다() {
            // given
            long id = 1L;
            long existingPoint = 200L;
            long pointToCharge = 100L;
            long expectedPoint = existingPoint + pointToCharge;

            UserPoint existingUserPoint = new UserPoint(id, existingPoint, System.currentTimeMillis());
            UserPoint updatedUserPoint = new UserPoint(id, expectedPoint, System.currentTimeMillis());

            when(userPointRepository.selectById(id))
                    .thenReturn(existingUserPoint);
            when(userPointRepository.insertOrUpdate(id, expectedPoint))
                    .thenReturn(updatedUserPoint);

            // when
            UserPoint actual = pointService.charge(id, new PointChargeRequest(pointToCharge));

            // then
            assertThat(actual.point())
                    .isEqualTo(expectedPoint);
        }
        /**
         * Test Case: 충전 후에는 포인트 충전 내역이 추가되어야 합니다.
         * 작성 이유:
         *  - 포인트 충전 시, 충전 내역이 기록되는지 검증합니다.
         * */
        @Test
        void 충전_후에는_포인트_충전_내역이_추가된다() {
            // given
            long id = 1L;
            long existingPoint = 200L;
            long pointToCharge = 100L;
            long expectedPoint = existingPoint + pointToCharge;

            UserPoint existingUserPoint = new UserPoint(id, existingPoint, System.currentTimeMillis());
            UserPoint updatedUserPoint = new UserPoint(id, expectedPoint, System.currentTimeMillis());

            when(userPointRepository.selectById(id))
                    .thenReturn(existingUserPoint);
            when(userPointRepository.insertOrUpdate(id, expectedPoint))
                    .thenReturn(updatedUserPoint);

            // when
            pointService.charge(id, new PointChargeRequest(pointToCharge));

            // then
            verify(pointHistoryRepository, times(1))
                    .insert(eq(id), eq(pointToCharge), eq(TransactionType.CHARGE), eq(updatedUserPoint.updateMillis()));
        }

        /**
         * Test Case: 포인트 충전 정책 검증을 통과하지 못하면 충전에 실패합니다.
         * 작성 이유
         *  - PointValidator 에서 예외가 발생하는 경우, 포인트 충전이 실패하는지 검증합니다.
         * */
        @Test
        void 포인트_충전_검증을_통과하지_못하면_충전에_실패한다() {
            // given
            long id = 1L;
            long existingPoint = 200L;
            long pointToCharge = 100L;

            UserPoint existingUserPoint = new UserPoint(id, existingPoint, System.currentTimeMillis());

            when(userPointRepository.selectById(id))
                    .thenReturn(existingUserPoint);
            doThrow(new IllegalArgumentException("Validation Fail"))
                    .when(pointValidator)
                    .validateForCharge(anyLong(), anyLong());

            // when
            assertThatThrownBy(() -> pointService.charge(id, new PointChargeRequest(pointToCharge)))
                    .isInstanceOf(IllegalArgumentException.class);

            // then
            verify(userPointRepository, never())
                    .insertOrUpdate(anyLong(), anyLong());
        }
    }

    @Nested
    @DisplayName("포인트 사용")
    class UsePointTest {
        /**
         * Test Case: 사용 후 금액은 (사용 전 금액 - 사용 금액)과 일치해야 합니다.
         * 작성 이유
         *  - 포인트 사용 기능이 정확하게 동작하는지 검증합니다.
         * */
        @Test
        void 사용_후_잔고는_사용_전_잔고에서_사용_금액을_뺀_값과_일치한다() {
            // given
            long id = 1L;
            long existingPoint = 200L;
            long pointToUse = 100L;
            long expectedPoint = existingPoint - pointToUse;

            UserPoint existingUserPoint = new UserPoint(id, existingPoint, System.currentTimeMillis());
            UserPoint updatedUserPoint = new UserPoint(id, expectedPoint, System.currentTimeMillis());

            when(userPointRepository.selectById(id))
                    .thenReturn(existingUserPoint);
            when(userPointRepository.insertOrUpdate(id, expectedPoint))
                    .thenReturn(updatedUserPoint);

            // when
            UserPoint actual = pointService.use(id, new PointUseRequest(pointToUse));

            // then
            assertThat(actual.point())
                    .isEqualTo(expectedPoint);
        }
        /**
         * Test Case: 사용 후에는 포인트 사용 내역이 추가되어야 합니다.
         * 작성 이유:
         *  - 포인트 사용 시, 사용 내역이 기록되는지 검증합니다.
         * */
        @Test
        void 사용_후에는_포인트_사용_내역이_추가된다() {
            // given
            long id = 1L;
            long existingPoint = 200L;
            long pointToUse = 100L;
            long expectedPoint = existingPoint - pointToUse;

            UserPoint existingUserPoint = new UserPoint(id, existingPoint, System.currentTimeMillis());
            UserPoint updatedUserPoint = new UserPoint(id, expectedPoint, System.currentTimeMillis());

            when(userPointRepository.selectById(id))
                    .thenReturn(existingUserPoint);
            when(userPointRepository.insertOrUpdate(id, expectedPoint))
                    .thenReturn(updatedUserPoint);

            // when
            pointService.use(id, new PointUseRequest(pointToUse));

            // then
            verify(pointHistoryRepository, times(1))
                    .insert(eq(id), eq(pointToUse), eq(TransactionType.USE), eq(updatedUserPoint.updateMillis()));
        }

        /**
         * Test Case: 포인트 사용 정책 검증을 통과하지 못하면 사용에 실패합니다.
         * 작성 이유
         *  - PointValidator 에서 예외가 발생하는 경우, 포인트 사용이 실패하는지 검증합니다.
         * */
        @Test
        void 포인트_사용_검증을_통과하지_못하면_사용에_실패한다() {
            // given
            long id = 1L;
            long existingPoint = 200L;
            long pointToUse = 300L;

            UserPoint existingUserPoint = new UserPoint(id, existingPoint, System.currentTimeMillis());

            when(userPointRepository.selectById(id))
                    .thenReturn(existingUserPoint);
            doThrow(new IllegalArgumentException("Validation Fail"))
                    .when(pointValidator)
                    .validateForUse(anyLong(), anyLong());

            // when
            assertThatThrownBy(() -> pointService.use(id, new PointUseRequest(pointToUse)))
                    .isInstanceOf(IllegalArgumentException.class);

            // then
            verify(userPointRepository, never())
                    .insertOrUpdate(anyLong(), anyLong());
        }

    }
}