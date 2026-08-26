package com.example.cnwasshu.domain.timetable.dto.request;

import com.example.cnwasshu.domain.timetable.entity.TimetableScheduleType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalTime;

public record TimetableScheduleRequest(
        @NotBlank
        @Size(max = 100)
        String clientScheduleId,

        @NotNull
        TimetableScheduleType scheduleType,

        Long activityId,
        Long restaurantId,
        Long reservationId,

        @NotBlank
        @Size(max = 100)
        String title,

        @NotNull
        LocalTime startTime,

        @NotNull
        LocalTime endTime,

        @Size(max = 2000)
        String memo,

        @NotNull
        @Min(1)
        Integer sortOrder
) {
}
