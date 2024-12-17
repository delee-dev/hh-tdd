package io.hhplus.tdd.point;

import org.springframework.stereotype.Component;

@Component
public class PointValidator {
    private final long MAX_POINT_AMOUNT = 1_000_000L;

    public void validateForCharge(long existingPoint, long pointToCharge) {
        if (pointToCharge <= 0) {
            throw new IllegalArgumentException("충전할 금액은 0보다 커야 합니다.");
        }
        if (existingPoint + pointToCharge > MAX_POINT_AMOUNT) {
            throw new IllegalArgumentException("충전 후 잔액은 100만을 넘을 수 없습니다.");
        }
    }
}
