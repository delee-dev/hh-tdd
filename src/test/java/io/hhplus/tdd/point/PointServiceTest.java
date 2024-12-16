package io.hhplus.tdd.point;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PointServiceTest {
    @InjectMocks
    private PointService pointService;
    @Mock
    private UserPointTable userPointTable;
    @Mock
    private PointHistoryTable pointHistoryTable;

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

            when(userPointTable.selectById(id))
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

            when(userPointTable.selectById(id))
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
        @DisplayName("사용자가 포인트를 충전 및 사용한 내역이 있다면, 해당 내역을 반환해야 합니다.")
        void getHistories_whenUserHasHistories_shouldReturnHistories() {
            // given
            long userId = 1L;

            List<PointHistory> expected = new ArrayList<>();
            PointHistory chargingHistory = new PointHistory(userId, 1L, 200, TransactionType.CHARGE, System.currentTimeMillis());
            PointHistory usingHistory = new PointHistory(userId, 2L, 100, TransactionType.USE, System.currentTimeMillis());
            expected.add(chargingHistory);
            expected.add(usingHistory);

            when(pointHistoryTable.selectAllByUserId(userId))
                    .thenReturn(expected);

            // when
            List<PointHistory> actual = pointService.getHistories(userId);

            //then
            assertThat(actual)
                    .as("시스템에 등록된 포인트 내역이 정확히 반환되어야 합니다.")
                    .usingRecursiveComparison()
                    .isEqualTo(expected);
        }

        /**
         * Test Case: 사용자가 시스템에 포인트를 충전 및 사용한 내역이 없다면, 빈 리스트를 반환해야 합니다.
         * 작성 이유
         *  - null 을 반환하거나 예외를 발생시켜서는 안됩니다.
         * */
        @Test
        @DisplayName("사용자가 시스템에 포인트를 충전 및 사용한 내역이 없다면, 빈 리스트를 반환해야 합니다.")
        void getHistories_whenUserHasNoHistories_shouldReturnEmptyList() {
            // given
            long userId = 1L;
            List<PointHistory> expected = Collections.emptyList();

            when(pointHistoryTable.selectAllByUserId(userId))
                    .thenReturn(expected);

            // when & then
            // 예외 검사
            assertThatCode(() -> pointService.getHistories(userId))
                    .as("충전 및 사용 내역이 없는 경우 빈 리스트가 반환되어야 합니다.")
                    .doesNotThrowAnyException();

            // 값 검사
            List<PointHistory> actual = pointService.getHistories(userId);

            assertThat(actual)
                    .as("충전 및 사용 내역이 없는 경우 빈 리스트가 반환되어야 합니다.")
                    .isNotNull()
                    .isEmpty();
        }
    }

}