package com.example.cnwasshu.domain.timetable.service;

import com.example.cnwasshu.domain.timetable.entity.ActivityOperatingType;

import java.time.LocalTime;

public record TimetableRestaurantInfo(
        Long restaurantId,
        ActivityOperatingType operatingType,
        LocalTime operatingStartTime,
        LocalTime operatingEndTime
) {
}
