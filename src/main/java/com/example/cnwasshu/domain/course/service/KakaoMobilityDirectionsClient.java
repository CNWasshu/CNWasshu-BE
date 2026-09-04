package com.example.cnwasshu.domain.course.service;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class KakaoMobilityDirectionsClient {

    private final RestClient.Builder restClientBuilder;

    @Value("${kakao-mobility.rest-api-key:}")
    private String restApiKey;

    @Value("${kakao-mobility.base-url:https://apis-navi.kakaomobility.com}")
    private String baseUrl;

    public Optional<RouteInfo> getCarRoute(
            BigDecimal originLatitude,
            BigDecimal originLongitude,
            BigDecimal destinationLatitude,
            BigDecimal destinationLongitude
    ) {
        if (!StringUtils.hasText(restApiKey)) {
            return Optional.empty();
        }

        try {
            JsonNode response = restClientBuilder.baseUrl(baseUrl).build()
                    .get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/v1/directions")
                            .queryParam("origin", originLongitude + "," + originLatitude)
                            .queryParam("destination", destinationLongitude + "," + destinationLatitude)
                            .queryParam("priority", "RECOMMEND")
                            .build())
                    .header("Authorization", "KakaoAK " + restApiKey)
                    .retrieve()
                    .body(JsonNode.class);

            JsonNode summary = response == null ? null : response.at("/routes/0/summary");
            if (summary == null || !summary.path("distance").canConvertToInt()
                    || !summary.path("duration").canConvertToInt()) {
                return Optional.empty();
            }
            return Optional.of(new RouteInfo(
                    summary.path("distance").intValue(),
                    summary.path("duration").intValue()
            ));
        } catch (RuntimeException exception) {
            log.warn("Kakao Mobility directions lookup failed; course detail will omit route info: {}",
                    exception.getMessage());
            return Optional.empty();
        }
    }

    public record RouteInfo(Integer distanceMeters, Integer travelTimeSeconds) {}
}
