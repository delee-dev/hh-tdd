package io.hhplus.tdd.point;

import io.hhplus.tdd.database.UserPointTable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PointServiceTest {
    @InjectMocks
    private PointService pointService;
    @Mock
    private UserPointTable userPointTable;

    @Nested
    @DisplayName("포인트 조회")
    class ReadTest {
        /**
         * Test Case: 사용자가 시스템에 포인트를 등록(충전)한 경우, 해당 UserPoint 객체를 반환해야 합니다.
         * 작성 이유
         *  - 반환된 UserPoint 객체의 값들이 정확히 일치하는지 확인합니다.
         * */
        @Test
        @DisplayName("시스템에 포인트를 충전한 유저는 정확한 UserPoint 객체를 반환해야 합니다.")
        void getPoint_WhenUserIsCharged_shouldReturnUserPoint() {
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
                    .as("시스템에 등록된 UserPoint 가 정확히 반환되어야 합니다.")
                    .usingRecursiveAssertion()
                    .isEqualTo(expected);
        }

        /**
         * Test Case: 사용자가 시스템에 포인트를 등록(충전)하지 않은 경우, 포인트가 0인 UserPoint 객체(기본 값)를 반환해야 합니다.
         * 작성 이유
         *  - 등록(충전)하지 않은 사용자에 대해 기본 값을 반환하는지 검증합니다.
         *  - 반환된 객체가 null 이거나, 실행 중 예외가 발생해서는 안되며 포인트는 0이어야 합니다.
         * */
        @Test
        @DisplayName("시스템에 포인트를 충전하지 않은 유저는 포인트가 0인 UserPoint 객체를 반환해야 합니다.")
        void getPoint_WhenUserIsNotCharged_shouldReturnZeroPoint() {
            // given
            long id = 1L;
            UserPoint expected = UserPoint.empty(id);

            when(userPointTable.selectById(id))
                    .thenReturn(expected);

            // when & then
            // 예외 검사
            assertThatCode(() -> pointService.getPoint(id))
                    .as("충전하지 않은 사용자의 경우 기본 값이 반환되어야 합니다.")
                    .doesNotThrowAnyException();

            // 필드 값 검사
            UserPoint actual = pointService.getPoint(id);

            assertThat(actual).as("충전하지 않은 사용자의 경우 기본 값이 반환되어야 합니다.").isNotNull();
            assertThat(actual.id()).as("기본 값의 id는 입력받은 id와 동일해야 합니다.").isEqualTo(expected.id());
            assertThat(actual.point()).as("기본 값의 point는 0이어야 합니다.").isEqualTo(expected.point());
        }
    }
}