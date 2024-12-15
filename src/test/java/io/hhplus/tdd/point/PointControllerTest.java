package io.hhplus.tdd.point;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
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
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PointController.class)
class PointControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    PointService pointService;

    @Test
    @DisplayName("특정 유저의 포인트를 조회한다.")
    public void getUserPointTest() throws Exception {
        // given
        long userId = 1L;

        // when
        UserPoint userPoint = new UserPoint(userId, 0, System.currentTimeMillis());
        when(pointService.getPoint(userId)).thenReturn(userPoint);

        mockMvc.perform(MockMvcRequestBuilders.get("/point/{userId}", userId)) // HTTP 요청을 생성 및 설정
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())                           // 검증
                .andExpect(jsonPath("$.point").exists())
                .andExpect(jsonPath("$.updateMillis").exists());

        // verify
        verify(pointService).getPoint(userId);
    }

    @Test
    @DisplayName("특정 유저의 포인트 충전/이용 내역을 조회한다.")
    public void getUserPointHistoryTest() throws Exception {
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
    @DisplayName("특정 유저의 포인트를 충전한다.")
    public void patchChargePointTest() throws Exception {
        long chargePoint = 100L;

        mockMvc.perform(MockMvcRequestBuilders.patch("/point/0/charge")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.valueOf(chargePoint)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.point").exists())
                .andExpect(jsonPath("$.updateMillis").exists());
    }

    @Test
    @DisplayName("특정 유저의 포인트를 사용한다.")
    public void patchUsePointTest() throws Exception {
        long chargePoint = 100L;

        mockMvc.perform(MockMvcRequestBuilders.patch("/point/0/use")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.valueOf(chargePoint)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.point").exists())
                .andExpect(jsonPath("$.updateMillis").exists());
    }
}