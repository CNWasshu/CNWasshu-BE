package com.example.cnwasshu.domain.course.service;

import com.example.cnwasshu.domain.course.dto.request.AiCourseRequest;
import com.example.cnwasshu.domain.course.dto.response.AiCourseRecommendResponse;
import com.example.cnwasshu.domain.course.dto.response.CourseItemResponse;
import com.example.cnwasshu.domain.course.exception.GeminiRecommendationException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class GeminiCourseClient {

    private static final String CLIENT_ERROR_MESSAGE = "Gemini API 호출에 실패했습니다.";
    private static final int MAX_ERROR_BODY_LOG_LENGTH = 4_000;

    private final RestClient.Builder restClientBuilder;
    private final ObjectMapper objectMapper;

    @Value("${gemini.api-key:}")
    private String apiKey;

    @Value("${gemini.model:gemini-3.6-flash}")
    private String model;

    @Value("${gemini.base-url:https://generativelanguage.googleapis.com/v1beta}")
    private String baseUrl;

    public AiCourseRecommendResponse recommend(AiCourseRequest request) {
        boolean apiKeyConfigured = StringUtils.hasText(apiKey);
        log.info("Gemini API key configured: {}", apiKeyConfigured);
        log.info("Gemini request endpoint: {}/models/{}:generateContent", baseUrl, model);

        if (!apiKeyConfigured) {
            log.error("Gemini request aborted: API key is not configured");
            throw new GeminiRecommendationException(CLIENT_ERROR_MESSAGE);
        }

        String responseBody;
        try {
            responseBody = restClientBuilder.baseUrl(baseUrl).build()
                    .post()
                    .uri("/models/{model}:generateContent", model)
                    .header("x-goog-api-key", apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(buildRequest(request))
                    .retrieve()
                    .body(String.class);
            log.info("Gemini response status: 200");
        } catch (RestClientResponseException e) {
            log.error("Gemini HTTP error response - status: {}, exception: {}, message: {}",
                    e.getStatusCode().value(), e.getClass().getName(), e.getMessage());
            log.error("Gemini error response: {}", safeErrorBody(e.getResponseBodyAsString()));
            throw new GeminiRecommendationException(CLIENT_ERROR_MESSAGE, e);
        } catch (RestClientException e) {
            log.error("Gemini HTTP request failed - exception: {}, message: {}",
                    e.getClass().getName(), e.getMessage(), e);
            throw new GeminiRecommendationException(CLIENT_ERROR_MESSAGE, e);
        }

        JsonNode response;
        try {
            response = objectMapper.readTree(responseBody);
        } catch (JsonProcessingException e) {
            log.error("Gemini HTTP 200 response JSON parsing failed - exception: {}, message: {}",
                    e.getClass().getName(), e.getMessage(), e);
            throw new GeminiRecommendationException(CLIENT_ERROR_MESSAGE, e);
        }

        String generatedCourseJson;
        try {
            generatedCourseJson = extractText(response);
        } catch (IllegalArgumentException e) {
            log.error("Gemini HTTP 200 response does not contain generated text - exception: {}, message: {}, response: {}",
                    e.getClass().getName(), e.getMessage(), safeErrorBody(responseBody));
            throw new GeminiRecommendationException(CLIENT_ERROR_MESSAGE, e);
        }

        try {
            GeminiCourseResult result = objectMapper.readValue(generatedCourseJson, GeminiCourseResult.class);
            return toResponse(result, request);
        } catch (JsonProcessingException e) {
            log.error("Gemini generated course JSON parsing failed - exception: {}, message: {}",
                    e.getClass().getName(), e.getMessage(), e);
            throw new GeminiRecommendationException(CLIENT_ERROR_MESSAGE, e);
        } catch (IllegalArgumentException e) {
            log.error("Gemini generated course validation failed - exception: {}, message: {}",
                    e.getClass().getName(), e.getMessage(), e);
            throw new GeminiRecommendationException(CLIENT_ERROR_MESSAGE, e);
        }
    }

    private String safeErrorBody(String body) {
        if (!StringUtils.hasText(body)) {
            return "<empty>";
        }
        String redacted = body.replace(apiKey, "[REDACTED]");
        return redacted.length() <= MAX_ERROR_BODY_LOG_LENGTH
                ? redacted
                : redacted.substring(0, MAX_ERROR_BODY_LOG_LENGTH) + "...(truncated)";
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

                날짜별로 지역 내 가까운 장소끼리 묶어 불필요한 이동을 줄이세요.
                이동 방식에 현실적으로 맞는 이동 시간과 식사 시간을 일정 사이에 확보하세요.
                점심(12:00~14:00)과 저녁(18:00~20:00) 시간대에는 식사 일정을 포함하거나 식사 시간을 비워두세요.
                여행 스타일이 아이와 함께라면 연령대가 특정되지 않았으므로 가족 친화적이고 무리 없는 장소를 선택하세요.
                각 일정은 09:00~22:00 안에 배치하고 같은 날 일정 시간이 겹치면 안 됩니다.
                dayNo는 첫날이 1이며 여행 일수 범위를 넘지 않아야 합니다.
                여러 날짜인 경우 모든 날짜를 dayNo별로 구분하고 하루 안에서는 시작 시간이 빠른 순서로 반환하세요.
                startTime과 endTime은 HH:mm 형식으로 작성하세요.
                memo에는 추천 이유와 다음 장소까지의 간단한 이동 안내를 포함하세요.
                실제로 존재하는 충청남도 장소만 추천하고 가상의 장소나 주소를 만들지 마세요.
                사용자가 선택한 희망 지역 안의 장소를 우선 추천하세요.
                각 장소의 실제 도로명 주소와 위도(latitude), 경도(longitude)를 반환하세요.
                latitude와 longitude는 문자열이 아닌 JSON number로 반환하세요.
                확인할 수 없는 영업시간이나 세부 정보는 단정하지 마세요.
                """.formatted(
                request.startDate(), request.endDate(), request.region(),
                request.peopleCount(), transportationLabel(request), travelStyleLabel(request));

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
                "address", Map.of("type", "string", "description", "실제 도로명 주소"),
                "latitude", Map.of("type", "number", "minimum", -90, "maximum", 90),
                "longitude", Map.of("type", "number", "minimum", -180, "maximum", 180),
                "memo", Map.of("type", "string", "description", "추천 이유와 이동 안내")
        );
        Map<String, Object> item = Map.of(
                "type", "object",
                "properties", itemProperties,
                "required", List.of(
                        "title", "dayNo", "startTime", "endTime",
                        "address", "latitude", "longitude", "memo")
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
        List<ValidatedGeminiItem> validatedItems = result.items().stream().map(item -> {
            if (item.dayNo() == null || item.dayNo() < 1 || item.dayNo() > totalDays) {
                throw new IllegalArgumentException("여행 기간을 벗어난 추천 일정입니다.");
            }
            if (!StringUtils.hasText(item.title())) {
                throw new IllegalArgumentException("추천 일정의 장소명이 비어 있습니다.");
            }
            LocalTime startTime = LocalTime.parse(item.startTime());
            LocalTime endTime = LocalTime.parse(item.endTime());
            if (!startTime.isBefore(endTime)) {
                throw new IllegalArgumentException("추천 일정의 종료 시간이 시작 시간보다 빠릅니다.");
            }
            if (startTime.isBefore(LocalTime.of(9, 0)) || endTime.isAfter(LocalTime.of(22, 0))) {
                throw new IllegalArgumentException("추천 일정은 09:00~22:00 사이여야 합니다.");
            }
            BigDecimal latitude = coordinateOrNull(item.latitude(), new BigDecimal("-90"), new BigDecimal("90"));
            BigDecimal longitude = coordinateOrNull(
                    item.longitude(), new BigDecimal("-180"), new BigDecimal("180"));
            if (latitude == null || longitude == null) {
                latitude = null;
                longitude = null;
            }
            return new ValidatedGeminiItem(
                    item.title(), item.dayNo(), startTime, endTime,
                    item.address(), latitude, longitude, item.memo());
        }).sorted(Comparator.comparing(ValidatedGeminiItem::dayNo)
                .thenComparing(ValidatedGeminiItem::startTime)
                .thenComparing(ValidatedGeminiItem::endTime))
                .toList();

        for (int i = 0; i < validatedItems.size(); i++) {
            for (int j = i + 1; j < validatedItems.size(); j++) {
                ValidatedGeminiItem a = validatedItems.get(i);
                ValidatedGeminiItem b = validatedItems.get(j);
                if (a.dayNo().equals(b.dayNo())
                        && a.startTime().isBefore(b.endTime())
                        && b.startTime().isBefore(a.endTime())) {
                    throw new IllegalArgumentException("Gemini 추천 일정의 시간이 겹칩니다.");
                }
            }
        }

        Map<Integer, Integer> sortOrders = new java.util.HashMap<>();
        List<CourseItemResponse> items = validatedItems.stream()
                .map(item -> new CourseItemResponse(
                        null,
                        null,
                        null,
                        null,
                        item.title(),
                        item.dayNo(),
                        item.startTime(),
                        item.endTime(),
                        sortOrders.merge(item.dayNo(), 1, Integer::sum),
                        item.address(),
                        item.latitude(),
                        item.longitude(),
                        item.memo(),
                        null,
                        null
                ))
                .toList();
        return new AiCourseRecommendResponse(result.suggestedCourseName(), items);
    }

    private String transportationLabel(AiCourseRequest request) {
        return switch (request.transportation()) {
            case CAR -> "자가용";
            case PUBLIC_TRANSIT -> "대중교통";
            case WALKING -> "도보";
        };
    }

    private BigDecimal coordinateOrNull(JsonNode value, BigDecimal minimum, BigDecimal maximum) {
        if (value == null || !value.isNumber()) {
            return null;
        }
        BigDecimal coordinate = value.decimalValue();
        return coordinate.compareTo(minimum) >= 0 && coordinate.compareTo(maximum) <= 0
                ? coordinate
                : null;
    }

    private String travelStyleLabel(AiCourseRequest request) {
        return switch (request.travelStyle()) {
            case HEALING -> "힐링";
            case WITH_CHILD -> "아이와 함께";
            case FOOD -> "먹거리 중심";
            case PHOTO_SPOT -> "사진 명소";
        };
    }

    private record GeminiCourseResult(String suggestedCourseName, List<GeminiCourseItem> items) {}

    private record GeminiCourseItem(
            String title,
            Integer dayNo,
            String startTime,
            String endTime,
            String address,
            JsonNode latitude,
            JsonNode longitude,
            String memo
    ) {}

    private record ValidatedGeminiItem(
            String title,
            Integer dayNo,
            LocalTime startTime,
            LocalTime endTime,
            String address,
            BigDecimal latitude,
            BigDecimal longitude,
            String memo
    ) {}
}
