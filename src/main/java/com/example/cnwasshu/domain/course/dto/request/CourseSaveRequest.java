package com.example.cnwasshu.domain.course.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;

/** 타임테이블(장바구니) 코스 저장 / AI 추천 코스 저장 공통 요청 바디 */
public record CourseSaveRequest(
        @NotBlank String courseName,
        @NotNull Integer peopleCount,
        @NotNull Boolean withChild,
        @NotNull LocalDate startDate,
        @NotNull LocalDate endDate,
        @NotEmpty @Valid List<CourseItemRequest> items
) {}