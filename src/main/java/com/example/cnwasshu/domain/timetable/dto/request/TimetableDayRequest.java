package com.example.cnwasshu.domain.timetable.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;

public record TimetableDayRequest(
        @NotNull
        @Min(1)
        @Max(7)
        Integer dayNo,

        @NotNull
        LocalDate date,

        @NotNull
        @Valid
        List<TimetableScheduleRequest> schedules
) {
}
