package io.hhplus.tdd.point.repository;

import io.hhplus.tdd.point.PointHistory;
import io.hhplus.tdd.point.TransactionType;
import io.hhplus.tdd.point.UserPoint;

import java.util.List;

public interface PointRepository {

    UserPoint getPoint(long userId);

    List<PointHistory> getHistory(long userId);

    UserPoint insertOrUpdate(long userId, long point);

    PointHistory insertHistory(long userId, long amount, TransactionType transactionType, long updateMillis);
}
