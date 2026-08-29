package com.example.cnwasshu.domain.timetable.service;

import java.util.Map;

public record TimetableReferenceData(
        Map<Long, TimetableActivityInfo> activities,
        Map<Long, TimetableRestaurantInfo> restaurants,
        Map<Long, TimetableReservationInfo> reservations
) {
}
