package io.hhplus.tdd.point;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.point.request.PointChargeRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class PointService {
    private final UserPointTable userPointTable;
    private final PointHistoryTable pointHistoryTable;


    /**
     * 포인트 조회 함수
     *
     * 행동 분석
     * 1. id 값을 파라미터로 넘겨 받는다.
     * 2. 포인트 조회 API 를 호출한다.
     * 3. 조회된 결과를 반환한다.
     *
     * Test Case
     * 1. 성공
     *  - 시스템에 포인트를 충전한 경우, 해당 UserPoint 객체를 반환한다.
     *  - 시스템에 포인트를 충전하지 않은 경우, 기본 값을 반환한다.
     **/
    public UserPoint getPoint(long id) {
        return userPointTable.selectById(id);
    }

    /**
     * 포인트 내역 조회 함수
     *
     * 행동 분석
     * 1. userId 값을 파라미터로 넘겨 받는다.
     * 2. 포인트 내역 조회 API 를 호출한다.
     * 3. 조회된 결과를 반환한다.
     *
     * Test Case
     * 1. 성공
     *  - 포인트 내역이 존재하는 경우, 해당 내역을 반환한다.
     *  - 포인트 내역이 존재하지 않는 경우, 빈 리스트를 반환한다.
     **/
    public List<PointHistory> getHistories(long userId) {
        return pointHistoryTable.selectAllByUserId(userId);
    }

    /**
     * 포인트를 충전하는 함수
     *
     * 정책
     * 1. 포인트는 최대 한도(1,000,000)를 초과할 수 없다.
     * 2. 충전할 금액은 0보다 큰 값이어야 한다.
     *
     * 행동 분석
     * 1. id 와 충전 예정 포인트를 파라미터로 넘겨받는다.
     * 2. 충전할 포인트가 정책(2)을 준수하는지 검사한다.
     * 3. 조회 API 로 기존 포인트를 조회한다.
     * 4. 충전 후의 포인트가 정책(1)을 준수하는지 검사한다.
     * 5. 충전 API 를 호출한다.
     * 6. 충전 내역 추가 API 를 호출한다.
     *
     * Test Case
     * 1. 성공
     *  - 충전 후 금액은 (충전 전 금액 + 충전 금액)과 일치한다.
     *  - 포인트 충전 내역이 추가된다.
     * 2. 실패
     *  - 파라미터로 받은 충전할 금액이 0보다 같거나 작으면 실패한다.
     *  - 충전 후 잔고가 최대 한도를 초과하면 실패한다.
     * */
    public UserPoint charge(long id, PointChargeRequest request) {
        UserPoint existingUserPoint = userPointTable.selectById(id);
        if (request.amount() <= 0) throw new RuntimeException("충전 금액은 0보다 큰 금액이어야 합니다.");
        if (existingUserPoint.point() + request.amount() > 1_000_000) throw new RuntimeException("포인트 최대 한도를 초과했습니다. 최대 한도는 1,000,000 입니다.");
        UserPoint result = userPointTable.insertOrUpdate(id, existingUserPoint.point() + request.amount());
        pointHistoryTable.insert(id, request.amount(), TransactionType.CHARGE, result.updateMillis());
        return result;
    }
}
