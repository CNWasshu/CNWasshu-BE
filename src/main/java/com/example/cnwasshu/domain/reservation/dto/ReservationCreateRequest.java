package com.example.cnwasshu.domain.reservation.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;

public record ReservationCreateRequest(

        @NotNull
        Long activityId,

        @NotNull
        LocalDate reservationDate,

        @NotNull
        LocalTime reservationTime,

        @NotNull
        @Min(1)
        Integer peopleCount,

        @NotNull
        Boolean withChild

) {
}