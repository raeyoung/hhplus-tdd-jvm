package io.hhplus.tdd.point;

import io.hhplus.tdd.point.exception.UserNotFoundException;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
    @DisplayName("존재하지 않은 userId의 회원으로 포인트를 조회할 경우 예외를 리턴한다.")
    public void getPointByNotExistUserId() {
        // given
        long userId = -1L;

        when(pointService.getPoint(userId))
                .thenThrow(new UserNotFoundException("Not found user. [id] = [" + userId + "]"));

        // when & then
        UserNotFoundException exception = assertThrows(
                UserNotFoundException.class,
                () -> pointService.getPoint(userId)
        );

        assertThat(exception)
                .hasMessageContaining("Not found user. [id] = [" + userId + "]");
    }

    @Test
    @DisplayName("특정 유저의 포인트를 조회할 경우 성공한다.")
    public void getPointByUserIdSuccessTest() {
        // given
        long userId = -1L;
        long point = 100L;

        // when
        UserPoint userPoint = new UserPoint(userId, point, System.currentTimeMillis());
        when(pointRepository.selectById(userId)).thenReturn(userPoint);

        UserPoint thenUserPoint = pointService.getPoint(userId);

        // then
        assertEquals(thenUserPoint.id(), userPoint.id());
        assertEquals(thenUserPoint.point(), userPoint.point());
        assertEquals(thenUserPoint.updateMillis(), userPoint.updateMillis());

        // verify
        verify(pointRepository).selectById(userId);
    }

    @Test
    @DisplayName("특정 유저의 포인트 충전/이용 내역을 조회할 경우 성공한다.")
    public void getUserPointHistorySuccessTest() {
        // given
        long userId = 1L;
        long id1 = 1L;
        long id2 = 2L;

        // when (예상값)
        List<PointHistory> historyList = List.of(
                new PointHistory(id1, userId, 1000, TransactionType.CHARGE, System.currentTimeMillis()),
                new PointHistory(id2, userId, 1000, TransactionType.USE, System.currentTimeMillis())
        );

        when(pointRepository.selectAllByUserId(userId)).thenReturn(historyList);

        List<PointHistory> result = pointService.getHistory(userId);

        // then
        assertNotNull(result);                              // 결과값 null 여부 확인
        assertEquals(historyList.size(), result.size());    // 결과값 크기가 기대값과 같은지 확인
        for (int i = 0; i < historyList.size(); i++) {      // historyList 와 result 일치 여부 확인
            PointHistory expected = historyList.get(i);
            PointHistory actual = result.get(i);

            assertEquals(expected.id(), actual.id());
            assertEquals(expected.userId(), actual.userId());
            assertEquals(expected.amount(), actual.amount());
            assertEquals(expected.type(), actual.type());
            assertEquals(expected.updateMillis(), actual.updateMillis());
        }

        // verify
        verify(pointRepository).selectAllByUserId(userId);        // pointRepository가 정확히 한 번 호출되었는지 검증
    }
}