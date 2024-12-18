package io.hhplus.tdd.point;

import io.hhplus.tdd.point.exception.InvalidAmountException;
import io.hhplus.tdd.point.exception.InvalidUserException;
import io.hhplus.tdd.point.repository.PointRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Service
@RequiredArgsConstructor
public class PointService {

    private final Map<Long, ReentrantLock> userLocks = new ConcurrentHashMap<>();
    private final PointRepository pointRepository;

    public UserPoint getPoint(long userId) {
        if(userId < 0) {
            throw new InvalidUserException("Invalid userId : " + userId);
        }
        return pointRepository.getPoint(userId);
    }

    public List<PointHistory> getHistory(long userId) {
        if(userId < 0) {
            throw new InvalidUserException("Invalid userId : " + userId);
        }
        return pointRepository.getHistory(userId);
    }

    public UserPoint charge(long userId, long point) {
        // 음수 여부 체크
        if(point < 0) {
            throw new InvalidAmountException("Invalid amount. Amount must be greater than 0. Requested amount: " + point);
        }

        // 1회 충전 금액이 1,000,000을 초과 여부 체크
        if (point > 1_000_000) {
            throw new InvalidAmountException("Max allowed charge is 1,000,000.");
        }

        // 사용자별 락을 생성 또는 조회
        final Lock lock = userLocks.computeIfAbsent(userId, id -> new ReentrantLock(true));
        lock.lock();    // 락 획득

        try {
            UserPoint getPoint = pointRepository.getPoint(userId);  // 포인트 조회
            if(getPoint == null) {
                throw new InvalidAmountException("User point not found");
            }

            // 총 포인트가 10,000,000을 초과
            long totalPoints = getPoint.point() + point;
            if (totalPoints > 10_000_000) {
                throw new InvalidAmountException("Total points exceed the limit of 10,000,000.");
            }
            UserPoint userPoint = pointRepository.insertOrUpdate(getPoint.id(), getPoint.point() + point);  // 포인트 업데이트
            pointRepository.insertHistory(userId, point, TransactionType.CHARGE, System.currentTimeMillis());     // 충전 내역 조회

            return userPoint;

        } finally {
            lock.unlock();  // 락 해제
        }
    }
}
