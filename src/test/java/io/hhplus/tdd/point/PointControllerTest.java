package io.hhplus.tdd.point;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PointController.class)
class PointControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("특정 유저의 포인트를 조회한다.")
    public void getUserPointTest() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/point/0")) // HTTP 요청을 생성 및 설정
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(0))
                .andExpect(jsonPath("$.point").value(0))
                .andExpect(jsonPath("$.updateMillis").value(0));
    }

    @Test
    @DisplayName("특정 유저의 포인트 충전/이용 내역을 조회한다.")
    public void getUserPointHistoryTest() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/point/1/histories")
                        .accept(MediaType.APPLICATION_JSON))    // GET 요청에 대한 Accept 헤더를 설정 (JSON 형식의 응답을 기대하는 것)
                .andExpect(jsonPath("$.length()").value(0));
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