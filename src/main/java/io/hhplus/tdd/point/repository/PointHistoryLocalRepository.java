package io.hhplus.tdd.point.repository;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.point.entity.PointHistory;
import io.hhplus.tdd.point.entity.type.TransactionType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class PointHistoryLocalRepository implements PointHistoryRepository {
    private final PointHistoryTable pointHistoryTable;

    @Override
    public PointHistory insert(long userId, long amount, TransactionType transactionType, long updateMillis) {
        return pointHistoryTable.insert(userId, amount, transactionType, updateMillis);
    }

    @Override
    public List<PointHistory> selectAllByUserId(long userId) {
        return pointHistoryTable.selectAllByUserId(userId);
    }
}
