package io.hhplus.tdd.point;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
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
}
