package com.example.cnwasshu.domain.timetable.service;

import java.time.LocalDate;
import java.time.LocalTime;

public record TimetableReservationInfo(
        Long reservationId,
        Long userId,
        Long activityId,
        LocalDate reservationDate,
        LocalTime reservationTime,
        String status
) {
}
