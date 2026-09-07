package com.example.cnwasshu.domain.weather.service;

import com.example.cnwasshu.domain.weather.ChungnamWeatherRegion;
import com.example.cnwasshu.domain.weather.WeatherApiProperties;
import com.example.cnwasshu.domain.weather.dto.WeatherResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WeatherService {

    private static final Duration CACHE_DURATION =
            Duration.ofMinutes(20);

    private final WeatherApiProperties weatherApiProperties;
    private final ObjectMapper objectMapper;

    private final RestClient restClient =
            RestClient.create();

    private List<WeatherResponse> cachedWeather;
    private LocalDateTime cachedAt;

    public synchronized List<WeatherResponse> getChungnamWeather() {
        if (isCacheValid()) {
            return cachedWeather;
        }

        List<WeatherResponse> weatherResponses =
                new ArrayList<>();

        for (ChungnamWeatherRegion region
                : ChungnamWeatherRegion.values()) {

            double temperature =
                    getCurrentTemperature(
                            region.getNx(),
                            region.getNy()
                    );

            String condition =
                    getCurrentCondition(
                            region.getNx(),
                            region.getNy()
                    );

            weatherResponses.add(
                    new WeatherResponse(
                            region.getRegionName(),
                            temperature,
                            condition
                    )
            );
        }

        cachedWeather =
                List.copyOf(weatherResponses);

        cachedAt =
                LocalDateTime.now();

        return cachedWeather;
    }

    private boolean isCacheValid() {
        if (cachedWeather == null
                || cachedAt == null) {
            return false;
        }

        Duration elapsed =
                Duration.between(
                        cachedAt,
                        LocalDateTime.now()
                );

        return elapsed.compareTo(
                CACHE_DURATION
        ) < 0;
    }

    private double getCurrentTemperature(
            int nx,
            int ny
    ) {
        LocalDateTime baseDateTime =
                resolveObservationBaseDateTime();

        for (int attempt = 0;
             attempt < 3;
             attempt++) {

            LocalDateTime requestDateTime =
                    baseDateTime.minusHours(attempt);

            URI uri =
                    createWeatherUri(
                            "/getUltraSrtNcst",
                            requestDateTime,
                            nx,
                            ny
                    );

            String response =
                    requestWeatherApi(uri);

            try {
                JsonNode root =
                        objectMapper.readTree(
                                response
                        );

                String resultCode =
                        root
                                .path("response")
                                .path("header")
                                .path("resultCode")
                                .asText();

                if (!"00".equals(resultCode)) {
                    continue;
                }

                JsonNode items =
                        root
                                .path("response")
                                .path("body")
                                .path("items")
                                .path("item");

                if (!items.isArray()
                        || items.isEmpty()) {
                    continue;
                }

                for (JsonNode item : items) {
                    if ("T1H".equals(
                            item
                                    .path("category")
                                    .asText()
                    )) {
                        return item
                                .path("obsrValue")
                                .asDouble();
                    }
                }

            } catch (Exception e) {
                throw new IllegalStateException(
                        "기상청 실황 데이터 처리에 실패했습니다.",
                        e
                );
            }
        }

        throw new IllegalStateException(
                "기온 데이터를 찾을 수 없습니다."
        );
    }

    private String getCurrentCondition(
            int nx,
            int ny
    ) {
        LocalDateTime baseDateTime =
                resolveForecastBaseDateTime();

        for (int attempt = 0;
             attempt < 4;
             attempt++) {

            LocalDateTime requestDateTime =
                    baseDateTime.minusHours(attempt);

            URI uri =
                    createWeatherUri(
                            "/getUltraSrtFcst",
                            requestDateTime,
                            nx,
                            ny
                    );

            String response =
                    requestWeatherApi(uri);

            try {
                JsonNode root =
                        objectMapper.readTree(
                                response
                        );

                String resultCode =
                        root
                                .path("response")
                                .path("header")
                                .path("resultCode")
                                .asText();

                if (!"00".equals(resultCode)) {
                    continue;
                }

                JsonNode items =
                        root
                                .path("response")
                                .path("body")
                                .path("items")
                                .path("item");

                if (!items.isArray()
                        || items.isEmpty()) {
                    continue;
                }

                String condition =
                        extractCondition(items);

                if (!"UNKNOWN".equals(condition)) {
                    return condition;
                }

            } catch (Exception e) {
                throw new IllegalStateException(
                        "기상청 초단기예보 데이터 처리에 실패했습니다.",
                        e
                );
            }
        }

        return "UNKNOWN";
    }

    private String extractCondition(
            JsonNode items
    ) {
        LocalDateTime now =
                LocalDateTime.now();

        LocalDateTime targetDateTime =
                null;

        String sky =
                null;

        for (JsonNode item : items) {
            String category =
                    item
                            .path("category")
                            .asText();

            if (!"SKY".equals(category)) {
                continue;
            }

            String fcstDate =
                    item
                            .path("fcstDate")
                            .asText();

            String fcstTime =
                    item
                            .path("fcstTime")
                            .asText();

            LocalDateTime forecastDateTime =
                    parseForecastDateTime(
                            fcstDate,
                            fcstTime
                    );

            if (forecastDateTime == null) {
                continue;
            }

            if (forecastDateTime.isBefore(now)) {
                continue;
            }

            if (targetDateTime == null
                    || forecastDateTime
                    .isBefore(targetDateTime)) {

                targetDateTime =
                        forecastDateTime;

                sky =
                        item
                                .path("fcstValue")
                                .asText();
            }
        }

        if (targetDateTime == null) {
            for (JsonNode item : items) {
                String category =
                        item
                                .path("category")
                                .asText();

                if (!"SKY".equals(category)) {
                    continue;
                }

                String fcstDate =
                        item
                                .path("fcstDate")
                                .asText();

                String fcstTime =
                        item
                                .path("fcstTime")
                                .asText();

                targetDateTime =
                        parseForecastDateTime(
                                fcstDate,
                                fcstTime
                        );

                sky =
                        item
                                .path("fcstValue")
                                .asText();

                break;
            }
        }

        if (targetDateTime == null
                || sky == null) {
            return "UNKNOWN";
        }

        String targetDate =
                targetDateTime.format(
                        DateTimeFormatter.ofPattern(
                                "yyyyMMdd"
                        )
                );

        String targetTime =
                targetDateTime.format(
                        DateTimeFormatter.ofPattern(
                                "HHmm"
                        )
                );

        String pty =
                null;

        for (JsonNode item : items) {
            String category =
                    item
                            .path("category")
                            .asText();

            if (!"PTY".equals(category)) {
                continue;
            }

            String fcstDate =
                    item
                            .path("fcstDate")
                            .asText();

            String fcstTime =
                    item
                            .path("fcstTime")
                            .asText();

            if (targetDate.equals(fcstDate)
                    && targetTime.equals(fcstTime)) {

                pty =
                        item
                                .path("fcstValue")
                                .asText();

                break;
            }
        }

        return convertCondition(
                sky,
                pty
        );
    }

    private LocalDateTime parseForecastDateTime(
            String date,
            String time
    ) {
        try {
            return LocalDateTime.parse(
                    date + time,
                    DateTimeFormatter.ofPattern(
                            "yyyyMMddHHmm"
                    )
            );
        } catch (Exception e) {
            return null;
        }
    }

    private String convertCondition(
            String sky,
            String pty
    ) {
        if (pty != null
                && !"0".equals(pty)) {

            return switch (pty) {
                case "3", "7" -> "SNOW";
                case "2", "6" -> "RAIN_SNOW";
                case "1", "4", "5" -> "RAIN";
                default -> "RAIN";
            };
        }

        return switch (sky) {
            case "1" -> "SUNNY";
            case "3", "4" -> "CLOUDY";
            default -> "UNKNOWN";
        };
    }

    private URI createWeatherUri(
            String path,
            LocalDateTime baseDateTime,
            int nx,
            int ny
    ) {
        String baseDate =
                baseDateTime.format(
                        DateTimeFormatter.ofPattern(
                                "yyyyMMdd"
                        )
                );

        String baseTime =
                baseDateTime.format(
                        DateTimeFormatter.ofPattern(
                                "HHmm"
                        )
                );

        return UriComponentsBuilder
                .fromUriString(
                        weatherApiProperties.baseUrl()
                                + path
                )
                .queryParam(
                        "ServiceKey",
                        weatherApiProperties
                                .serviceKey()
                )
                .queryParam(
                        "pageNo",
                        1
                )
                .queryParam(
                        "numOfRows",
                        1000
                )
                .queryParam(
                        "dataType",
                        "JSON"
                )
                .queryParam(
                        "base_date",
                        baseDate
                )
                .queryParam(
                        "base_time",
                        baseTime
                )
                .queryParam(
                        "nx",
                        nx
                )
                .queryParam(
                        "ny",
                        ny
                )
                .build(true)
                .toUri();
    }

    private String requestWeatherApi(
            URI uri
    ) {
        return restClient
                .get()
                .uri(uri)
                .retrieve()
                .body(String.class);
    }

    private LocalDateTime resolveObservationBaseDateTime() {
        LocalDateTime now =
                LocalDateTime.now();

        if (now.getMinute() < 40) {
            now =
                    now.minusHours(1);
        }

        return now
                .withMinute(0)
                .withSecond(0)
                .withNano(0);
    }

    private LocalDateTime resolveForecastBaseDateTime() {
        LocalDateTime now =
                LocalDateTime.now();

        if (now.getMinute() < 45) {
            now =
                    now.minusHours(1);
        }

        return now
                .withMinute(30)
                .withSecond(0)
                .withNano(0);
    }
}