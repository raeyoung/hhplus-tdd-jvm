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
    public void 특정_유저의_포인트_조회를_성공한다() {
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
    void 유저가_존재하지_않는_경우_실패한다() {
        // given
        long userId = -1L;

        // when & then
        assertThrows(InvalidUserException.class, () -> {
            pointService.getPoint(userId);
        });

        // verify
        verify(pointRepository, never()).getPoint(anyLong());
    }

    @Test
    public void 특정_유저의_포인트_충전_및_이용내역_조회에_성공한다() {
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
    void 특정_유저의_포인트_충전을_성공한다() {
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
        assertThat(result).isNotNull();                                         // 결과값 null 여부 확인
        assertThat(result.id()).isEqualTo(userId);                              // id가 기대값과 같은지 확인
        assertThat(result.point()).isEqualTo(existingPoints + chargeAmount);   // 포인트가 기대값과 같은지 확인

        // verify
        verify(pointRepository, times(1)).getPoint(userId);                                                                   // getPoint 가 정확히 1번 호출되었는지 검증
        verify(pointRepository, times(1)).insertOrUpdate(userId, existingPoints + chargeAmount);                        // insertOrUpdate 가 정확히 1번 호출되었는지 검증
        verify(pointRepository, times(1)).insertHistory(eq(userId), eq(chargeAmount), eq(TransactionType.CHARGE), anyLong()); // insertHistory 가 정확히 1번 호출되었는지 검증
    }

    @Test
    void 포인트_충전금액이_0보다_작으면_실패한다() {
        // given
        long userId = 1L;
        long chargeAmount = -100L; // 충전 금액이 음수일 경우

        // when & then
        assertThrows(InvalidAmountException.class, () -> {
            pointService.charge(userId, chargeAmount);
        });
    }

    @Test
    void 총_포인트가_10_000_000을_초과하면_실패한다() {
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
    void 포인트_1회_충전금액이_1_000_000을_초과하면_실패한다() {
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

    @Test
    public void 특정_유저의_포인트를_사용을_성공한다() {
        // Given
        long userId = 1L;
        long usePoint = 100L;           // 사용할 포인트
        long totalPoint = 500L;         // 사용 전 포인트
        long remainingPoint = 400L;     // 사용 후 남은 포인트

        UserPoint mockUserPoint = new UserPoint(userId, totalPoint, System.currentTimeMillis());
        UserPoint updatedUserPoint = new UserPoint(userId, remainingPoint, System.currentTimeMillis());

        // When
        when(pointRepository.getPoint(userId)).thenReturn(mockUserPoint);
        when(pointRepository.insertOrUpdate(userId, remainingPoint)).thenReturn(updatedUserPoint);

        UserPoint result = pointService.use(userId, usePoint);

        // Then
        assertThat(result).isNotNull();                         // 결과값 null 여부 확인
        assertThat(result.id()).isEqualTo(userId);              // id가 기대값과 같은지 확인
        assertThat(result.point()).isEqualTo(remainingPoint);   // 포인트가 기대값과 같은지 확인

        // verify
        verify(pointRepository).getPoint(userId);
        verify(pointRepository).insertOrUpdate(userId, remainingPoint);
    }

    @Test
    public void 사용할_포인트가_유효하지_않을_경우_실패한다() {
        // given
        long userId = 1L;
        long point = -1000L;
        UserPoint userPoint = mock(UserPoint.class);
        when(pointRepository.getPoint(userId)).thenReturn(userPoint);

        // when & then
        assertThrows(InvalidAmountException.class, () -> {
            pointService.use(userId, point);
        });

        // verify
        verify(pointRepository, never()).insertOrUpdate(anyLong(), anyLong());  // insertOrUpdate 호출 여부 검증
    }

    @Test
    public void 포인트_잔액이_부족할_경우_포인트_사용이_실패한다() {
        // Given
        long userId = 1L;
        long point = 1000L;

        // 현재 포인트는 500으로 설정
        UserPoint currentUserPoint = new UserPoint(userId, 500L, System.currentTimeMillis());
        when(pointRepository.getPoint(userId)).thenReturn(currentUserPoint);

        // when & then
        assertThrows(InvalidAmountException.class, () -> {
            pointService.use(userId, point);
        });

        // verify
        verify(pointRepository, never()).insertOrUpdate(anyLong(), anyLong());
    }
}