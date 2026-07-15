package com.example.health_records;

import com.example.health_records.dto.HealthRequest;
import com.example.health_records.entity.Health;
import com.example.health_records.enums.Conditions;
import com.example.health_records.repository.HealthRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class HealthIntegrationTest {
    private Health mayRecord;
    private Health juneRecord;
    private Health julyRecord;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private HealthRepository healthRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mayRecord = healthRepository.save(Health.create(60.0, Conditions.BAD, 8.0,
                "体調悪い", LocalDate.of(2026, 5, 10)));
        juneRecord = healthRepository.save(Health.create(65.0, Conditions.GOOD, 8.0,
                "よく寝れた", LocalDate.of(2026, 6, 10)));
        julyRecord = healthRepository.save(Health.create(70.0, Conditions.NORMAL, 7.0,
                "太った", LocalDate.of(2026, 7, 10)));
    }

    @Test
    void POST_health_records_記録が登録される() throws Exception {
        HealthRequest request = new HealthRequest();
        ReflectionTestUtils.setField(request, "weight", 65.0);
        ReflectionTestUtils.setField(request, "condition", Conditions.GOOD);
        ReflectionTestUtils.setField(request, "sleepTime", 8.0);
        ReflectionTestUtils.setField(request, "memo", "よく寝れた");
        ReflectionTestUtils.setField(request, "recordDate", LocalDate.of(2026, 7, 10));
        String json = objectMapper.writeValueAsString(request);

        String responseBody =
                mockMvc.perform(post("/health-records")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.weight").value(request.getWeight()))
                .andExpect(jsonPath("$.condition").value(String.valueOf(request.getCondition())))
                .andExpect(jsonPath("$.sleepTime").value(request.getSleepTime()))
                .andExpect(jsonPath("$.memo").value(request.getMemo()))
                .andExpect(jsonPath("$.recordDate").value(String.valueOf(request.getRecordDate())))
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode responseJson = objectMapper.readTree(responseBody);
        Long id = responseJson.get("id").asLong();
        Health savedHealth = healthRepository.findById(id).orElseThrow();
        assertThat(healthRepository.findAll()).hasSize(4);
        assertThat(savedHealth.getWeight()).isEqualTo(request.getWeight());
        assertThat(savedHealth.getCondition()).isEqualTo(request.getCondition());
        assertThat(savedHealth.getSleepTime()).isEqualTo(request.getSleepTime());
        assertThat(savedHealth.getMemo()).isEqualTo(request.getMemo());
        assertThat(savedHealth.getRecordDate()).isEqualTo(request.getRecordDate());
    }

    @Test
    void GET_health_records_登録記録を全件取得() throws Exception {
        String responseBody =
                mockMvc.perform(get("/health-records"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andReturn()
                .getResponse()
                .getContentAsString();
        JsonNode responses = objectMapper.readTree(responseBody);

        List<String> recordDates = new ArrayList<>();
        for (JsonNode node : responses) {
            recordDates.add(node.get("recordDate").asString());
        }
        assertThat(recordDates)
                .containsExactlyInAnyOrder(
                        String.valueOf(mayRecord.getRecordDate()),
                        String.valueOf(juneRecord.getRecordDate()),
                        String.valueOf(julyRecord.getRecordDate()));
    }

    @Test
    void GET_health_records_記録を絞り込み取得_両方指定() throws Exception{
        LocalDate from = LocalDate.of(2026, 6, 1);
        LocalDate to = LocalDate.of(2026, 7, 31);
        String responseBody =
                mockMvc.perform(get("/health-records")
                                .param("from", String.valueOf(from))
                                .param("to", String.valueOf(to)))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.length()").value(2))
                        .andReturn()
                        .getResponse()
                        .getContentAsString();
        JsonNode responses = objectMapper.readTree(responseBody);

        List<String> recordDates = new ArrayList<>();
        for (JsonNode node : responses) {
            recordDates.add(node.get("recordDate").asString());
        }
        assertThat(recordDates)
                .containsExactlyInAnyOrder(
                        String.valueOf(juneRecord.getRecordDate()),
                        String.valueOf(julyRecord.getRecordDate()));
    }

    @Test
    void GET_health_records_記録を絞り込み取得_fromのみ指定() throws Exception{
        LocalDate from = LocalDate.of(2026, 6, 1);
        String responseBody =
                mockMvc.perform(get("/health-records")
                                .param("from", String.valueOf(from)))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.length()").value(2))
                        .andReturn()
                        .getResponse()
                        .getContentAsString();
        JsonNode responses = objectMapper.readTree(responseBody);

        List<String> recordDates = new ArrayList<>();
        for (JsonNode node : responses) {
            recordDates.add(node.get("recordDate").asString());
        }
        assertThat(recordDates)
                .containsExactlyInAnyOrder(
                        String.valueOf(juneRecord.getRecordDate()),
                        String.valueOf(julyRecord.getRecordDate()));
    }

    @Test
    void GET_health_records_記録を絞り込み取得_toのみ指定() throws Exception{
        LocalDate to = LocalDate.of(2026, 6, 30);
        String responseBody =
                mockMvc.perform(get("/health-records")
                                .param("to", String.valueOf(to)))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.length()").value(2))
                        .andReturn()
                        .getResponse()
                        .getContentAsString();
        JsonNode responses = objectMapper.readTree(responseBody);

        List<String> recordDates = new ArrayList<>();
        for (JsonNode node : responses) {
            recordDates.add(node.get("recordDate").asString());
        }
        assertThat(recordDates)
                .containsExactlyInAnyOrder(
                        String.valueOf(mayRecord.getRecordDate()),
                        String.valueOf(juneRecord.getRecordDate()));
    }
}
