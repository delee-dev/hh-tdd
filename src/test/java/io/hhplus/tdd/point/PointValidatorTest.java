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

    @Nested
    @DisplayName("포인트 사용 시 검증 테스트")
    class ValidateForUseTest {
        /**
         * Test Case: 사용할 금액이 0보다 작거나 같으면 사용에 실패합니다.
         * 작성 이유
         *  - "사용할 금액은 0 보다 큰 값이어야 한다."는 정책을 만족하는지 확인합니다.
         * */
        @Test
        void 사용할_금액이_0보다_작거나_같으면_사용에_실패한다() {
            // given
            long existingPoint = 100L;
            long pointToUse = -100L;

            // when & then
            assertThatThrownBy(() -> pointValidator.validateForUse(existingPoint, pointToUse))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        /**
         * Test Case: 사용 금액이 충전된 금액을 초과하면 사용에 실패합니다.
         * 작성 이유
         *  - "충전된 금액보다 큰 금액을 사용할 수 없다."는 정책을 만족하는지 확인합니다.
         * */
        @Test
        void 사용_금액이_충전된_금액을_초과하면_사용에_실패한다() {
            // given
            long existingPoint = 200L;
            long pointToUse = 300L;

            // when & then
            assertThatThrownBy(() -> pointValidator.validateForUse(existingPoint, pointToUse))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }
}