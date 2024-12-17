package io.hhplus.tdd.point;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class PointValidatorTest {
    @InjectMocks
    private PointValidator pointValidator;

    @Nested
    @DisplayName("포인트 충전 시 검증 테스트")
    class ValidateForCharge {
        /**
         * Test Case: 충전할 금액이 0과 작거나 같으면 충전에 실패합니다.
         * 작성 이유
         *  - "충전할 금액은 0보다 큰 값이어야 한다"는 정책을 만족하는지 확인합니다.
         * */
        @Test
        void 충전할_금액이_0과_작거나_같으면_충전을_실패한다() {
            // given
            long existingPoint = 100L;
            long pointToCharge = -100L;

            // when & then
            assertThatThrownBy(() -> pointValidator.validateForCharge(existingPoint, pointToCharge))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        /**
         * Test Case: 충전 후 잔고가 최대 한도(1,000,000)를 초과하면 충전에 실패합니다.
         * 작성 이유
         *  - "포인트는 최대 한도(1,000,000)를 초과할 수 없다"는 정책을 만족하는지 확인합니다.
         * */
        @Test
        void 충전_후_잔고가_최대_한도를_초과하면_충전을_실패한다() {
            // given
            long existingPoint = 999_999L;
            long pointToCharge = 100L;

            // when & then
            assertThatThrownBy(() -> pointValidator.validateForCharge(existingPoint, pointToCharge))
                    .isInstanceOf(IllegalArgumentException.class);
        }

    }

}