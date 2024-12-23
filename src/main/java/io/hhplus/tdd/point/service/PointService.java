package io.hhplus.tdd.point.service;

import io.hhplus.tdd.point.dto.PointChargeRequest;
import io.hhplus.tdd.point.dto.PointUseRequest;
import io.hhplus.tdd.point.entity.PointHistory;
import io.hhplus.tdd.point.entity.UserPoint;
import io.hhplus.tdd.point.entity.type.TransactionType;
import io.hhplus.tdd.point.repository.PointHistoryRepository;
import io.hhplus.tdd.point.repository.UserPointRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

@RequiredArgsConstructor
@Service
public class PointService {
    private final UserPointRepository userPointRepository;
    private final PointHistoryRepository pointHistoryRepository;
    private final PointValidator pointValidator;
    private final LockManager lockManager;

    public UserPoint getPoint(long id) {
        ReentrantLock lock = lockManager.getLock(id);
        lock.lock();
        try {
            return userPointRepository.selectById(id);
        } finally {
            lock.unlock();
        }
    }

    public List<PointHistory> getHistories(long userId) {
        ReentrantLock lock = lockManager.getLock(userId);
        lock.lock();
        try {
            return pointHistoryRepository.selectAllByUserId(userId);
        } finally {
            lock.unlock();
        }
    }

    public UserPoint charge(long id, PointChargeRequest request) {
        ReentrantLock lock = lockManager.getLock(id);
        lock.lock();

        try {
            UserPoint existingUserPoint = userPointRepository.selectById(id);

            long beforePoint = existingUserPoint.point();
            long pointToCharge = request.amount();
            long afterPoint = beforePoint + pointToCharge;

            // 정책 검증
            pointValidator.validateForCharge(beforePoint, pointToCharge);
            // 포인트 충전
            UserPoint result = userPointRepository.insertOrUpdate(id, afterPoint);
            // 충전 내역 등록
            pointHistoryRepository.insert(id, pointToCharge, TransactionType.CHARGE, result.updateMillis());

            return result;
        } finally {
            lock.unlock();
        }
    }

    public UserPoint use(long id, PointUseRequest request) {
        ReentrantLock lock = lockManager.getLock(id);
        lock.lock();
        try {
            UserPoint existingUserPoint = userPointRepository.selectById(id);

            long beforePoint = existingUserPoint.point();
            long pointToUse = request.amount();
            long afterPoint = beforePoint - pointToUse;

            // 정책 검증
            pointValidator.validateForUse(beforePoint, pointToUse);
            // 포인트 사용
            UserPoint result = userPointRepository.insertOrUpdate(id, afterPoint);
            // 사용 내역 등록
            pointHistoryRepository.insert(id, pointToUse, TransactionType.USE, result.updateMillis());

            return result;
        } finally {
            lock.unlock();
        }
    }
}
