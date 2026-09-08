package com.example.cnwasshu.domain.weather.dto;

public record WeatherResponse(
        String regionName,
        double temperature,
        String condition
) {
}