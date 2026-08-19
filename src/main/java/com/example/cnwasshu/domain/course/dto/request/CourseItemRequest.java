package com.example.cnwasshu.domain.course.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
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
        @NotNull Integer sortOrder,
        @Size(max = 500) String address,
        @DecimalMin("-90.0") @DecimalMax("90.0") BigDecimal latitude,
        @DecimalMin("-180.0") @DecimalMax("180.0") BigDecimal longitude,
        String memo
) {}
