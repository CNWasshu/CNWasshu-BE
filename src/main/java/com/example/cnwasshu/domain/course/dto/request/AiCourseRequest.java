package com.example.cnwasshu.domain.course.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

/** Gemini 코스 추천 조건 */
public record AiCourseRequest(
        @NotNull LocalDate startDate,
        @NotNull LocalDate endDate,
        @NotBlank String region,
        @NotNull @Min(1) Integer peopleCount,
        @NotNull Transportation transportation,
        @NotNull TravelStyle travelStyle
) {}
