package io.hhplus.tdd.point;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.hhplus.tdd.point.exception.InvalidAmountException;
import io.hhplus.tdd.point.exception.InvalidUserException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PointController.class)
class PointControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    PointService pointService;

    @Test
    public void 특정_유저의_포인트_조회에_성공한다() throws Exception {
        // given
        long userId = 1L;

        // when
        UserPoint userPoint = new UserPoint(userId, 0, System.currentTimeMillis());
        when(pointService.getPoint(userId)).thenReturn(userPoint);

        // then
        mockMvc.perform(MockMvcRequestBuilders.get("/point/{userId}", userId)) // HTTP 요청을 생성 및 설정
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())                           // 검증
                .andExpect(jsonPath("$.point").exists())
                .andExpect(jsonPath("$.updateMillis").exists());

        // verify
        verify(pointService).getPoint(userId);
    }

    @Test
    public void 특정_유저의_포인트_충전_및_이용내역_조회에_성공한다() throws Exception {
        // given
        long userId = 1L;
        long id1 = 1L;
        long id2 = 2L;

        // when
        List<PointHistory> historyList = List.of(
                new PointHistory(id1, userId, 1000, TransactionType.CHARGE, System.currentTimeMillis()),
                new PointHistory(id2, userId, 1000, TransactionType.USE, System.currentTimeMillis())
        );

        when(pointService.getHistory(userId)).thenReturn(historyList);

        // then
        mockMvc.perform(get("/point/{userId}/histories", userId))   // HTTP 요청을 생성 및 설정
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())                 // 검증
                .andExpect(jsonPath("$[0]").exists())
                .andExpect(jsonPath("$[1]").exists())
                .andExpect(jsonPath("$[0].id").exists())
                .andExpect(jsonPath("$[0].userId").exists())
                .andExpect(jsonPath("$[0].amount").exists())
                .andExpect(jsonPath("$[0].type").exists())
                .andExpect(jsonPath("$[0].updateMillis").exists())
                .andExpect(jsonPath("$[1].id").exists())
                .andExpect(jsonPath("$[1].userId").exists())
                .andExpect(jsonPath("$[1].amount").exists())
                .andExpect(jsonPath("$[1].type").exists())
                .andExpect(jsonPath("$[1].updateMillis").exists())
                .andDo(print());

        // verify
        verify(pointService).getHistory(userId);
    }

    @Test
    public void 특정_유저의_포인트_충전에_성공한다() throws Exception {
        // given
        long userId = 1L;
        long chargePoint = 100L;
        long totalPoint = 500L;

        UserPoint mockUserPoint = new UserPoint(userId, totalPoint, System.currentTimeMillis());    // 예상 반환값 설정

        // when
        when(pointService.charge(userId, chargePoint)).thenReturn(mockUserPoint);

        // then
        mockMvc.perform(patch("/point/{id}/charge", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.valueOf(chargePoint)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId))
                .andExpect(jsonPath("$.point").value(mockUserPoint.point()))
                .andExpect(jsonPath("$.updateMillis").value(mockUserPoint.updateMillis()));
    }

    @Test
    public void 음수_값을_충전할_경우_실패한다() throws Exception {
        // given
        long userId = 1L;
        long point = -1000L;

        // when
        when(pointService.charge(userId, point))
                .thenThrow(new InvalidAmountException(
                    "Invalid amount. Amount must be greater than 0. Requested amount: " + point)
                );
        // then
        mockMvc.perform(patch("/point/{userId}/charge", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(point)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    public void 특정_유저의_포인트_사용을_성공한다() throws Exception {
        // Given
        long userId = 1L;
        long usedPoint = 100L;
        long totalPoint = 1000L;
        UserPoint mockUserPoint = new UserPoint(userId, totalPoint, System.currentTimeMillis()); // 예상 반환값 설정

        // when
        when(pointService.use(userId, usedPoint)).thenReturn(mockUserPoint);

        // then
        mockMvc.perform(patch("/point/{id}/use", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.valueOf(usedPoint)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId))
                .andExpect(jsonPath("$.point").value(mockUserPoint.point()))
                .andExpect(jsonPath("$.updateMillis").value(mockUserPoint.updateMillis()));
    }
}