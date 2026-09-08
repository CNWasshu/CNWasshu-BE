package com.example.cnwasshu.domain.weather;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "weather.api")
public record WeatherApiProperties(
        String serviceKey,
        String baseUrl
) {
}