package com.example.health_records.controller;

import com.example.health_records.dto.HealthRequest;
import com.example.health_records.dto.HealthUpdateRequest;
import com.example.health_records.dto.MonthlyStatsProjection;
import com.example.health_records.entity.Health;
import com.example.health_records.enums.Conditions;
import com.example.health_records.exception.HealthNotFoundException;
import com.example.health_records.service.HealthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;
import static org.mockito.BDDMockito.given;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.willThrow;

import java.time.LocalDate;
import java.util.List;

@WebMvcTest(HealthController.class)
public class HealthControllerTest {
    private Health mayRecord;
    private Health juneRecord;
    private Health julyRecord;

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    HealthService healthService;

    @Autowired
    ObjectMapper objectMapper;

    private static class MonthlyStatsProjectionTest implements MonthlyStatsProjection{
        private final Integer year;
        private final Integer month;
        private final Double avgWeight;

        MonthlyStatsProjectionTest(Integer year, Integer month, Double avgWeight) {
            this.year = year;
            this.month = month;
            this.avgWeight = avgWeight;
        }

        @Override
        public Integer getYear() { return year; }

        @Override
        public Integer getMonth() { return month; }

        @Override
        public Double getAvgWeight() { return avgWeight; }
    }

    @BeforeEach
    void setUp() {
        mayRecord = Health.create(
                70.0, Conditions.BAD, 6.0,
                "寝れなかった", LocalDate.of(2026, 5, 9));
        juneRecord = Health.create(
                65.0, Conditions.GOOD, 8.0,
                "よく寝れた", LocalDate.of(2026, 6, 9));
        julyRecord = Health.create(
                60.0, Conditions.NORMAL, 7.0,
                "ちょー痩せた", LocalDate.of(2026, 7, 9));
    }

    @Test
    void createHealth_正常系_201() throws Exception {
        HealthRequest request = new HealthRequest();
        ReflectionTestUtils.setField(request, "weight", 65.0);
        ReflectionTestUtils.setField(request, "condition", Conditions.GOOD);
        ReflectionTestUtils.setField(request, "sleepTime", 8.0);
        ReflectionTestUtils.setField(request, "memo", "よく寝れた");
        ReflectionTestUtils.setField(request, "recordDate", LocalDate.of(2026, 7, 10));
        Health createdHealth = Health.create(request.getWeight(), request.getCondition(),
                request.getSleepTime(), request.getMemo(), request.getRecordDate());
        String json =objectMapper.writeValueAsString(request);
        given(healthService.createHealth(any(HealthRequest.class))).willReturn(createdHealth);

        mockMvc.perform(post("/health-records")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.weight").value(request.getWeight()))
                .andExpect(jsonPath("$.condition").value(request.getCondition().toString()))
                .andExpect(jsonPath("$.sleepTime").value(request.getSleepTime()))
                .andExpect(jsonPath("$.memo").value(request.getMemo()))
                .andExpect(jsonPath("$.recordDate").value(request.getRecordDate().toString()));

        then(healthService).should().createHealth(any(HealthRequest.class));
    }

    @Test
    void createHealth_異常系_400_weightがnull() throws Exception {
        HealthRequest request = new HealthRequest();
        ReflectionTestUtils.setField(request, "condition", Conditions.GOOD);
        ReflectionTestUtils.setField(request, "sleepTime", 8.0);
        ReflectionTestUtils.setField(request, "recordDate", LocalDate.of(2026, 7, 10));
        String json = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/health-records")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());

        then(healthService).should(never()).createHealth(any(HealthRequest.class));
    }

    @Test
    void createHealth_異常系_400_weight1未満() throws Exception {
        HealthRequest request = new HealthRequest();
        ReflectionTestUtils.setField(request, "weight", 0.0);
        ReflectionTestUtils.setField(request, "condition", Conditions.GOOD);
        ReflectionTestUtils.setField(request, "sleepTime", 8.0);
        ReflectionTestUtils.setField(request, "recordDate", LocalDate.of(2026, 7, 10));
        String json = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/health-records")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isBadRequest());

        then(healthService).should(never()).createHealth(any(HealthRequest.class));
    }

    @Test
    void createHealth_異常系_400_conditionがnull() throws Exception {
        HealthRequest request = new HealthRequest();
        ReflectionTestUtils.setField(request, "weight", 65.0);
        ReflectionTestUtils.setField(request, "sleepTime", 8.0);
        ReflectionTestUtils.setField(request, "recordDate", LocalDate.of(2026, 7, 10));
        String json = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/health-records")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());

        then(healthService).should(never()).createHealth(any(HealthRequest.class));
    }

    @Test
    void createHealth_異常系_400_sleepTimeがnull() throws Exception {
        HealthRequest request = new HealthRequest();
        ReflectionTestUtils.setField(request, "weight", 65.0);
        ReflectionTestUtils.setField(request, "condition", Conditions.GOOD);
        ReflectionTestUtils.setField(request, "recordDate", LocalDate.of(2026, 7, 10));
        String json = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/health-records")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());

        then(healthService).should(never()).createHealth(any(HealthRequest.class));
    }

    @Test
    void createHealth_異常系_400_memo200文字以上() throws Exception {
        HealthRequest request = new HealthRequest();
        ReflectionTestUtils.setField(request, "weight", 65.0);
        ReflectionTestUtils.setField(request, "condition", Conditions.GOOD);
        ReflectionTestUtils.setField(request, "sleepTime", 8.0);
        ReflectionTestUtils.setField(request, "memo", "あ".repeat(201));
        ReflectionTestUtils.setField(request, "recordDate", LocalDate.of(2026, 7, 10));
        String json = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/health-records")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());

        then(healthService).should(never()).createHealth(any(HealthRequest.class));
    }

    @Test
    void createHealth_異常系_400_recordDateがnull() throws Exception {
        HealthRequest request = new HealthRequest();
        ReflectionTestUtils.setField(request, "weight", 65.0);
        ReflectionTestUtils.setField(request, "condition", Conditions.GOOD);
        ReflectionTestUtils.setField(request, "sleepTime", 8.0);
        String json = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/health-records")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());

        then(healthService).should(never()).createHealth(any(HealthRequest.class));
    }


    @Test
    void getHealth_正常系_200_全件取得() throws Exception {
        given(healthService.findAll()).willReturn(List.of(mayRecord, juneRecord, julyRecord));

        mockMvc.perform(get("/health-records"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].recordDate").value(mayRecord.getRecordDate().toString()))
                .andExpect(jsonPath("$[1].recordDate").value(juneRecord.getRecordDate().toString()))
                .andExpect(jsonPath("$[2].recordDate").value(julyRecord.getRecordDate().toString()));

        then(healthService).should().findAll();
    }

    @Test
    void getHealth_正常系_200_空リスト取得() throws Exception {
        given(healthService.findAll()).willReturn(List.of());

        mockMvc.perform(get("/health-records"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());

        then(healthService).should().findAll();
    }

    @Test
    void getHealth_正常系_200_指定した年月の記録一覧が返る_両方指定() throws Exception {
        LocalDate from = LocalDate.of(2026, 6, 1);
        LocalDate to = LocalDate.of(2026, 7, 31);
        given(healthService.getHealthRecordsInRange(from, to))
                .willReturn(List.of(juneRecord, julyRecord));

        mockMvc.perform(get("/health-records")
                        .param("from", from.toString())
                        .param("to", to.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].recordDate")
                        .value(juneRecord.getRecordDate().toString()))
                .andExpect(jsonPath("$[1].recordDate")
                        .value(julyRecord.getRecordDate().toString()));

        then(healthService).should().getHealthRecordsInRange(from, to);
    }

    @Test
    void getHealth_正常系_200_指定した年月の記録一覧が返る_fromのみ指定() throws Exception {
        LocalDate from = LocalDate.of(2026, 6, 1);
        given(healthService.getHealthRecordsInRange(from, null))
                .willReturn(List.of(juneRecord, julyRecord));

        mockMvc.perform(get("/health-records")
                        .param("from", from.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].recordDate")
                        .value(juneRecord.getRecordDate().toString()))
                .andExpect(jsonPath("$[1].recordDate")
                        .value(julyRecord.getRecordDate().toString()));

        then(healthService).should().getHealthRecordsInRange(from, null);
    }

    @Test
    void getHealth_正常系_200_指定した年月の記録一覧が返る_toのみ指定() throws Exception {
        LocalDate to = LocalDate.of(2026, 6, 30);
        given(healthService.getHealthRecordsInRange(null, to))
                .willReturn(List.of(mayRecord, juneRecord));

        mockMvc.perform(get("/health-records")
                        .param("to", to.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].recordDate")
                        .value(mayRecord.getRecordDate().toString()))
                .andExpect(jsonPath("$[1].recordDate")
                        .value(juneRecord.getRecordDate().toString()));

        then(healthService).should().getHealthRecordsInRange(null, to);
    }

    @Test
    void getMonthlyStats_正常系_200_指定した年月の平均体重が返る() throws Exception {
        Integer year = 2026;
        Integer month = 7;
        Double avgWeight = 62.5;
        MonthlyStatsProjection response = new MonthlyStatsProjectionTest(year, month, avgWeight);
        given(healthService.getMonthlyStats(year, month)).willReturn(response);

        mockMvc.perform(get("/health-records/stats")
                        .param("year", String.valueOf(year))
                        .param("month", String.valueOf(month)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.year").value(year))
                .andExpect(jsonPath("$.month").value(month))
                .andExpect(jsonPath("$.avgWeight").value(avgWeight));
        then(healthService).should().getMonthlyStats(year, month);
    }

    @Test
    void getMonthlyStats_異常系_404_HealthNotFoundExceptionがスローされる() throws Exception {
        Integer year = 2026;
        Integer month = 4;
        given(healthService.getMonthlyStats(year, month))
                .willThrow(new HealthNotFoundException(
                        year + "年" + month + "月のデータが見つかりません"));

        mockMvc.perform(get("/health-records/stats")
                        .param("year", String.valueOf(year))
                        .param("month", String.valueOf(month)))
                .andExpect(status().isNotFound())
                .andExpect(content().string("指定された記録がありません"));
        then(healthService).should().getMonthlyStats(year, month);
    }

    @Test
    void getMonthlyStats_異常系_400_month1未満() throws Exception {
        mockMvc.perform(get("/health-records/stats")
                        .param("year", "2026")
                        .param("month", "0"))
                .andExpect(status().isBadRequest());
        then(healthService).should(never()).getMonthlyStats(anyInt(), anyInt());
    }

    @Test
    void getMonthlyStats_異常系_400_month13以上() throws Exception {
        mockMvc.perform(get("/health-records/stats")
                        .param("year", "2026")
                        .param("month", "13"))
                .andExpect(status().isBadRequest());
        then(healthService).should(never()).getMonthlyStats(anyInt(), anyInt());
    }

    @Test
    void updateHealth_正常系_200_更新された記録が返る() throws Exception {
        HealthUpdateRequest request = new HealthUpdateRequest();
        ReflectionTestUtils.setField(request, "weight", 70.0);
        ReflectionTestUtils.setField(request, "condition", Conditions.BAD);
        ReflectionTestUtils.setField(request, "sleepTime", 6.0);
        ReflectionTestUtils.setField(request, "memo", "あまり眠れなかった");
        Health savedHealth = Health.create(request.getWeight(), request.getCondition(),
                request.getSleepTime(), request.getMemo(),
                LocalDate.of(2026, 7, 10));
        String json = objectMapper.writeValueAsString(request);
        given(healthService.updateHealth(anyLong(), any(HealthUpdateRequest.class)))
                .willReturn(savedHealth);

        mockMvc.perform(patch("/health-records/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.weight").value(request.getWeight()))
                .andExpect(jsonPath("$.condition").value(String.valueOf(request.getCondition())))
                .andExpect(jsonPath("$.sleepTime").value(request.getSleepTime()))
                .andExpect(jsonPath("$.memo").value(request.getMemo()));
        then(healthService).should().updateHealth(anyLong(), any(HealthUpdateRequest.class));
    }

    @Test
    void updateHealth_異常系_404_HealthNotFoundExceptionがスローされる() throws Exception {
        HealthUpdateRequest request = new HealthUpdateRequest();
        String json = objectMapper.writeValueAsString(request);
        given(healthService.updateHealth(anyLong(), any(HealthUpdateRequest.class)))
                .willThrow(new HealthNotFoundException(1L));

        mockMvc.perform(patch("/health-records/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isNotFound())
                .andExpect(content().string("指定された記録がありません"));
        then(healthService).should().updateHealth(anyLong(), any(HealthUpdateRequest.class));
    }

    @Test
    void updateHealth_異常系_400_IDが数値でない() throws Exception {
        HealthUpdateRequest request = new HealthUpdateRequest();
        String json = objectMapper.writeValueAsString(request);

        mockMvc.perform(patch("/health-records/abc")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
        then(healthService).should(never()).updateHealth(anyLong(), any(HealthUpdateRequest.class));
    }

    @Test
    void updateHealth_異常系_400_weight1未満() throws Exception {
        HealthUpdateRequest request = new HealthUpdateRequest();
        ReflectionTestUtils.setField(request, "weight", 0.0);
        String json = objectMapper.writeValueAsString(request);

        mockMvc.perform(patch("/health-records/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());

        then(healthService).should(never()).updateHealth(anyLong(), any(HealthUpdateRequest.class));
    }

    @Test
    void updateHealth_異常系_400_memo200文字以上() throws Exception {
        HealthUpdateRequest request = new HealthUpdateRequest();
        ReflectionTestUtils.setField(request, "memo", "あ".repeat(201));
        String json = objectMapper.writeValueAsString(request);

        mockMvc.perform(patch("/health-records/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());

        then(healthService).should(never()).updateHealth(anyLong(), any(HealthUpdateRequest.class));
    }

    @Test
    void deleteHealth_正常系_204_記録が削除される() throws Exception {
        mockMvc.perform(delete("/health-records/1"))
                .andExpect(status().isNoContent());
        then(healthService).should().deleteHealth(1L);
    }

    @Test
    void deleteHealth_異常系_400_IDが数値でない() throws Exception {
        mockMvc.perform(delete("/health-records/abc"))
                .andExpect(status().isBadRequest());
        then(healthService).should(never()).deleteHealth(anyLong());
    }

    @Test
    void deleteHealth_異常系_404_HealthNotFoundExceptionがスローされる() throws Exception {
        willThrow(new HealthNotFoundException(1L))
                .given(healthService)
                        .deleteHealth(1L);

        mockMvc.perform(delete("/health-records/1"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("指定された記録がありません"));
        then(healthService).should().deleteHealth(1L);
    }
}
