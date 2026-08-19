package com.example.cnwasshu.domain.course.service;

import com.example.cnwasshu.domain.course.dto.request.AiCourseRequest;
import com.example.cnwasshu.domain.course.dto.response.AiCourseRecommendResponse;
import com.example.cnwasshu.domain.course.dto.response.CourseItemResponse;
import com.example.cnwasshu.domain.course.exception.GeminiRecommendationException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.LocalTime;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class GeminiCourseClient {

    private final RestClient.Builder restClientBuilder;
    private final ObjectMapper objectMapper;

    @Value("${gemini.api-key:}")
    private String apiKey;

    @Value("${gemini.model:gemini-2.5-flash}")
    private String model;

    @Value("${gemini.base-url:https://generativelanguage.googleapis.com/v1beta}")
    private String baseUrl;

    public AiCourseRecommendResponse recommend(AiCourseRequest request) {
        if (!StringUtils.hasText(apiKey)) {
            throw new GeminiRecommendationException("GEMINI_API_KEY가 설정되지 않았습니다.");
        }

        try {
            JsonNode response = restClientBuilder.baseUrl(baseUrl).build()
                    .post()
                    .uri("/models/{model}:generateContent", model)
                    .header("x-goog-api-key", apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(buildRequest(request))
                    .retrieve()
                    .body(JsonNode.class);

            String json = extractText(response);
            GeminiCourseResult result = objectMapper.readValue(json, GeminiCourseResult.class);
            return toResponse(result, request);
        } catch (RestClientException e) {
            throw new GeminiRecommendationException("Gemini API 호출에 실패했습니다.", e);
        } catch (JsonProcessingException | IllegalArgumentException e) {
            throw new GeminiRecommendationException("Gemini 추천 결과를 해석할 수 없습니다.", e);
        }
    }

    private Map<String, Object> buildRequest(AiCourseRequest request) {
        String prompt = """
                당신은 대한민국 국내 여행 일정 전문가입니다.
                아래 조건에 맞는 현실적인 여행 코스를 한국어로 작성하세요.
                - 여행 기간: %s ~ %s
                - 희망 지역: %s
                - 인원: %d명
                - 이동 방식: %s
                - 여행 스타일: %s

                날짜별로 동선을 가깝게 구성하고 식사와 이동 시간을 고려하세요.
                각 일정은 09:00~22:00 안에 배치하고 같은 날 일정 시간이 겹치면 안 됩니다.
                dayNo는 첫날이 1이며 여행 일수 범위를 넘지 않아야 합니다.
                startTime과 endTime은 HH:mm 형식으로 작성하세요.
                memo에는 추천 이유와 다음 장소까지의 간단한 이동 안내를 포함하세요.
                확인할 수 없는 영업시간이나 세부 정보는 단정하지 마세요.
                """.formatted(
                request.startDate(), request.endDate(), request.region(),
                request.peopleCount(), request.transportation(), request.travelStyle());

        return Map.of(
                "contents", List.of(Map.of("parts", List.of(Map.of("text", prompt)))),
                "generationConfig", Map.of(
                        "temperature", 0.7,
                        "responseMimeType", "application/json",
                        "responseSchema", responseSchema()
                )
        );
    }

    private Map<String, Object> responseSchema() {
        Map<String, Object> itemProperties = Map.of(
                "title", Map.of("type", "string", "description", "방문 장소 또는 활동 이름"),
                "dayNo", Map.of("type", "integer", "minimum", 1),
                "startTime", Map.of("type", "string", "description", "HH:mm 형식"),
                "endTime", Map.of("type", "string", "description", "HH:mm 형식"),
                "memo", Map.of("type", "string", "description", "추천 이유와 이동 안내")
        );
        Map<String, Object> item = Map.of(
                "type", "object",
                "properties", itemProperties,
                "required", List.of("title", "dayNo", "startTime", "endTime", "memo")
        );
        return Map.of(
                "type", "object",
                "properties", Map.of(
                        "suggestedCourseName", Map.of("type", "string"),
                        "items", Map.of("type", "array", "items", item, "minItems", 1)
                ),
                "required", List.of("suggestedCourseName", "items")
        );
    }

    private String extractText(JsonNode response) {
        if (response == null) {
            throw new IllegalArgumentException("Gemini 응답이 비어 있습니다.");
        }
        JsonNode text = response.at("/candidates/0/content/parts/0/text");
        if (!text.isTextual() || !StringUtils.hasText(text.asText())) {
            throw new IllegalArgumentException("Gemini 응답에 추천 결과가 없습니다.");
        }
        return text.asText();
    }

    private AiCourseRecommendResponse toResponse(GeminiCourseResult result, AiCourseRequest request) {
        if (result == null || !StringUtils.hasText(result.suggestedCourseName())
                || result.items() == null || result.items().isEmpty()) {
            throw new IllegalArgumentException("Gemini 추천 결과의 필수 값이 없습니다.");
        }

        long totalDays = request.startDate().datesUntil(request.endDate().plusDays(1)).count();
        Map<Integer, Integer> sortOrders = new java.util.HashMap<>();
        List<CourseItemResponse> items = result.items().stream().map(item -> {
            if (item.dayNo() == null || item.dayNo() < 1 || item.dayNo() > totalDays) {
                throw new IllegalArgumentException("여행 기간을 벗어난 추천 일정입니다.");
            }
            LocalTime startTime = LocalTime.parse(item.startTime());
            LocalTime endTime = LocalTime.parse(item.endTime());
            if (!startTime.isBefore(endTime)) {
                throw new IllegalArgumentException("추천 일정의 종료 시간이 시작 시간보다 빠릅니다.");
            }
            int sortOrder = sortOrders.merge(item.dayNo(), 1, Integer::sum);
            return new CourseItemResponse(
                    null, null, null, item.title(), item.dayNo(), startTime, endTime,
                    item.memo(), sortOrder
            );
        }).toList();

        for (int i = 0; i < items.size(); i++) {
            for (int j = i + 1; j < items.size(); j++) {
                CourseItemResponse a = items.get(i);
                CourseItemResponse b = items.get(j);
                if (a.dayNo().equals(b.dayNo())
                        && a.startTime().isBefore(b.endTime())
                        && b.startTime().isBefore(a.endTime())) {
                    throw new IllegalArgumentException("Gemini 추천 일정의 시간이 겹칩니다.");
                }
            }
        }
        return new AiCourseRecommendResponse(result.suggestedCourseName(), items);
    }

    private record GeminiCourseResult(String suggestedCourseName, List<GeminiCourseItem> items) {}

    private record GeminiCourseItem(
            String title,
            Integer dayNo,
            String startTime,
            String endTime,
            String memo
    ) {}
}
