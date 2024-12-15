package io.hhplus.tdd.point.repository;

import io.hhplus.tdd.point.PointHistory;
import io.hhplus.tdd.point.UserPoint;

import java.util.List;

public interface PointRepository {

    UserPoint selectById(long userId);

    List<PointHistory> selectAllByUserId(long userId);
}
