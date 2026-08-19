package com.example.cnwasshu.domain.course.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalTime;

/**
 * 코스 안의 일정 하나.
 * - 직접 만든 일정 -> activityId, reservationId 둘 다 null
 * - 예약 없이 담은 액티비티 -> activityId만 존재
 * - 예약이 필요한 액티비티 -> activityId, reservationId 둘 다 존재
 */
public record CourseItemRequest(
        Long activityId,
        Long reservationId,
        @NotBlank String title,
        @NotNull Integer dayNo,
        @NotNull LocalTime startTime,
        @NotNull LocalTime endTime,
        String memo,
        @NotNull Integer sortOrder
) {}