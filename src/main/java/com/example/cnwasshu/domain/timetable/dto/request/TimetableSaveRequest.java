package com.example.cnwasshu.domain.timetable.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;

public record TimetableSaveRequest(
        @NotBlank
        @Size(max = 100)
        String timetableName,

        @NotNull
        @Min(1)
        Integer peopleCount,

        @NotNull
        Boolean withChild,

        @NotNull
        LocalDate startDate,

        @NotNull
        LocalDate endDate,

        @NotEmpty
        @Valid
        List<TimetableDayRequest> days
) {
}
