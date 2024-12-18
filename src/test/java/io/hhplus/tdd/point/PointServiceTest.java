package io.hhplus.tdd.point;

import io.hhplus.tdd.point.exception.InvalidAmountException;
import io.hhplus.tdd.point.exception.InvalidUserException;
import io.hhplus.tdd.point.repository.PointRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PointServiceTest {

    @Mock
    private PointRepository pointRepository;

    @InjectMocks
    private PointService pointService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("특정 유저의 포인트를 조회할 경우 성공한다.")
    public void getPointByUserIdSuccessTest() {
        // given
        long userId = 1L;
        long point = 100L;

        // when
        UserPoint userPoint = new UserPoint(userId, point, System.currentTimeMillis());
        when(pointRepository.getPoint(userId)).thenReturn(userPoint);

        UserPoint thenUserPoint = pointService.getPoint(userId);

        // then
        assertThat(thenUserPoint.id()).isEqualTo(userPoint.id());
        assertThat(thenUserPoint.point()).isEqualTo(userPoint.point());
        assertThat(thenUserPoint.updateMillis()).isEqualTo(userPoint.updateMillis());

        // verify
        verify(pointRepository).getPoint(userId);
    }

    @Test
    @DisplayName("특정 유저의 포인트 충전/이용 내역을 조회할 경우 성공한다.")
    public void getUserPointHistorySuccessTest() {
        // given
        long userId = 1L;
        long id1 = 1L;
        long id2 = 2L;

        // when
        List<PointHistory> historyList = List.of(
                new PointHistory(id1, userId, 1000, TransactionType.CHARGE, System.currentTimeMillis()),
                new PointHistory(id2, userId, 1000, TransactionType.USE, System.currentTimeMillis())
        );

        when(pointRepository.getHistory(userId)).thenReturn(historyList);

        List<PointHistory> result = pointService.getHistory(userId);

        // then
        assertThat(result).isNotNull();                                      // 결과값 null 여부 확인
        assertThat(result).hasSize(historyList.size());                      // 결과값 크기가 기대값과 같은지 확인
        for (int i = 0; i < historyList.size(); i++) {                        // historyList 와 result 일치 여부 확인
            PointHistory expected = historyList.get(i);
            PointHistory actual = result.get(i);

            assertThat(actual.id()).isEqualTo(expected.id());
            assertThat(actual.userId()).isEqualTo(expected.userId());
            assertThat(actual.amount()).isEqualTo(expected.amount());
            assertThat(actual.type()).isEqualTo(expected.type());
            assertThat(actual.updateMillis()).isEqualTo(expected.updateMillis());
        }

        // verify
        verify(pointRepository).getHistory(userId);                          // pointRepository 가 정확히 한 번 호출되었는지 검증
    }

    @Test
    @DisplayName("특정 유저의 포인트를 충전을 성공한다.")
    void chargePointSuccessTest() {
        // given
        long userId = 1L;
        long chargeAmount = 100L;
        long existingPoints = 200L;

        UserPoint existingUserPoint = new UserPoint(userId, existingPoints, System.currentTimeMillis());
        UserPoint updatedUserPoint = new UserPoint(userId, existingPoints + chargeAmount, System.currentTimeMillis());

        // Mock 설정
        when(pointRepository.getPoint(userId)).thenReturn(existingUserPoint);
        when(pointRepository.insertOrUpdate(userId, existingPoints + chargeAmount)).thenReturn(updatedUserPoint);

        // when
        UserPoint result = pointService.charge(userId, chargeAmount);

        // then
        assertThat(result).isNotNull();                                        // 결과값 null 여부 확인
        assertThat(result.id()).isEqualTo(userId);                             // id가 기대값과 같은지 확인
        assertThat(result.point()).isEqualTo(existingPoints + chargeAmount);   // 포인트가 기대값과 같은지 확인

        // verify
        verify(pointRepository, times(1)).getPoint(userId);                    // getPoint 가 정확히 1번 호출되었는지 검증
        verify(pointRepository, times(1)).insertOrUpdate(userId, existingPoints + chargeAmount); // insertOrUpdate 가 정확히 1번 호출되었는지 검증
        verify(pointRepository, times(1)).insertHistory(eq(userId), eq(chargeAmount), eq(TransactionType.CHARGE), anyLong()); // insertHistory 가 정확히 1번 호출되었는지 검증
    }

    @Test
    @DisplayName("포인트 충전 금액이 0보다 작으면 실패한다.")
    void chargeAmountLessThanZero() {
        // given
        long userId = 1L;
        long chargeAmount = -100L; // 충전 금액이 음수일 경우

        // when & then
        assertThrows(InvalidAmountException.class, () -> {
            pointService.charge(userId, chargeAmount);
        });
    }

    @Test
    @DisplayName("총 포인트가 10,000,000을 초과하면 실패한다.")
    void totalPointsExceedsLimit() {
        // given
        long userId = 1L;
        long currentPoints = 9_900_000L; // 현재 포인트가 9,900,000일 때
        long chargeAmount = 200_000L;    // 200,000 포인트 충전 요청

        UserPoint existingUserPoint = new UserPoint(userId, currentPoints, System.currentTimeMillis());
        UserPoint updatedUserPoint = new UserPoint(userId, currentPoints + chargeAmount, System.currentTimeMillis());

        // Mock 설정
        when(pointRepository.getPoint(userId)).thenReturn(existingUserPoint);
        when(pointRepository.insertOrUpdate(userId, currentPoints + chargeAmount)).thenReturn(updatedUserPoint);

        // when & then
        assertThrows(InvalidAmountException.class, () -> {
            pointService.charge(userId, chargeAmount);
        });
    }

    @Test
    @DisplayName("1회 충전 금액이 1,000,000을 초과하면 실패한다.")
    void chargeAmountExceedsLimit() {
        // given
        long userId = 1L;
        long currentPoints = 500_000L;  // 현재 포인트가 500,000일 때
        long chargeAmount = 1_200_000L; // 1,200,000 포인트 충전 요청

        UserPoint existingUserPoint = new UserPoint(userId, currentPoints, System.currentTimeMillis());

        // Mock 설정
        when(pointRepository.getPoint(userId)).thenReturn(existingUserPoint);

        // when & then
        assertThrows(InvalidAmountException.class, () -> {
            pointService.charge(userId, chargeAmount);
        });
    }
}