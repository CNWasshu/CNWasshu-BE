package com.example.cnwasshu.domain.weather.controller;

import com.example.cnwasshu.domain.weather.dto.WeatherResponse;
import com.example.cnwasshu.domain.weather.service.WeatherService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/home/weather")
public class WeatherController {

    private final WeatherService weatherService;

    @GetMapping
    public List<WeatherResponse> getWeather() {
        return weatherService.getChungnamWeather();
    }
}