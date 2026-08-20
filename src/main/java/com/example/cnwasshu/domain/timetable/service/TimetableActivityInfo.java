package com.example.cnwasshu.domain.timetable.service;

import com.example.cnwasshu.domain.timetable.entity.ActivityOperatingType;

import java.time.LocalTime;

public record TimetableActivityInfo(
        Long activityId,
        ActivityOperatingType operatingType,
        LocalTime operatingStartTime,
        LocalTime operatingEndTime,
        Integer durationMinutes,
        Boolean reservationRequired
) {
}
