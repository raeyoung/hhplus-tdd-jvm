package io.hhplus.tdd.point;

import io.hhplus.tdd.point.repository.PointRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PointService {

    private final PointRepository pointRepository;

    public UserPoint getPoint(long userId) {
        UserPoint userPoint = pointRepository.selectById(userId);
        return userPoint;
    }

    public List<PointHistory> getHistory(long userId) {
        List<PointHistory> historyList = pointRepository.selectAllByUserId(userId);
        return historyList;
    }
}
