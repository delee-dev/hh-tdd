package io.hhplus.tdd.point;

import io.hhplus.tdd.database.UserPointTable;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class PointService {
    private final UserPointTable userPointTable;
    public UserPoint getPoint(long id) {
        return userPointTable.selectById(id);
    }
}
