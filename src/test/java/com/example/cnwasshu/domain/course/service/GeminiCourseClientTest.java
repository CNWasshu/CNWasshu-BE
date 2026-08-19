package com.example.cnwasshu.domain.course.service;

import com.example.cnwasshu.domain.course.dto.request.AiCourseRequest;
import com.example.cnwasshu.domain.course.dto.request.Transportation;
import com.example.cnwasshu.domain.course.dto.request.TravelStyle;
import com.example.cnwasshu.domain.course.exception.GeminiRecommendationException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

class GeminiCourseClientTest {

    @Test
    void keepsScheduleButNullsInvalidCoordinatePair() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        GeminiCourseClient client = client(builder);
        server.expect(once(), requestTo(
                        "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.6-flash:generateContent"))
                .andRespond(org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess("""
                        {"candidates":[{"content":{"parts":[{"text":"{\\"suggestedCourseName\\":\\"공주 코스\\",\\"items\\":[{\\"title\\":\\"공산성\\",\\"dayNo\\":1,\\"startTime\\":\\"10:00\\",\\"endTime\\":\\"11:00\\",\\"address\\":\\"충남 공주시 금성동 53-51\\",\\"latitude\\":\\"not-a-number\\",\\"longitude\\":127.1234567,\\"memo\\":\\"성곽 관람\\"}]}"}]}}]}
                        """, MediaType.APPLICATION_JSON));

        var result = client.recommend(request());

        assertThat(result.items()).hasSize(1);
        assertThat(result.items().get(0).address()).isEqualTo("충남 공주시 금성동 53-51");
        assertThat(result.items().get(0).latitude()).isNull();
        assertThat(result.items().get(0).longitude()).isNull();
        server.verify();
    }

    @Test
    void convertsGeminiHttpErrorToCommonClientMessage() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        GeminiCourseClient client = client(builder);

        server.expect(once(), requestTo(
                        "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.6-flash:generateContent"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("x-goog-api-key", "test-api-key"))
                .andRespond(withStatus(HttpStatus.TOO_MANY_REQUESTS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("""
                                {"error":{"code":429,"status":"RESOURCE_EXHAUSTED","message":"quota exceeded"}}
                                """));

        assertThatThrownBy(() -> client.recommend(request()))
                .isInstanceOf(GeminiRecommendationException.class)
                .hasMessage("Gemini API 호출에 실패했습니다.");
        server.verify();
    }

    private GeminiCourseClient client(RestClient.Builder builder) {
        GeminiCourseClient client = new GeminiCourseClient(builder, new ObjectMapper());
        ReflectionTestUtils.setField(client, "apiKey", "test-api-key");
        ReflectionTestUtils.setField(client, "model", "gemini-3.6-flash");
        ReflectionTestUtils.setField(client, "baseUrl", "https://generativelanguage.googleapis.com/v1beta");
        return client;
    }

    private AiCourseRequest request() {
        return new AiCourseRequest(
                LocalDate.of(2026, 9, 10),
                LocalDate.of(2026, 9, 10),
                "공주시",
                2,
                Transportation.CAR,
                TravelStyle.HEALING
        );
    }
}
