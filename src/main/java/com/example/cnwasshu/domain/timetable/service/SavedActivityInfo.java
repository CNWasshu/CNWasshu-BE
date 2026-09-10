package com.example.cnwasshu.domain.timetable.service;

import com.example.cnwasshu.domain.timetable.entity.ActivityOperatingType;

import java.time.LocalTime;

public record SavedActivityInfo(
        Long activityId,
        String title,
        String thumbnailUrl,
        String region,
        ActivityOperatingType operatingType,
        LocalTime operatingStartTime,
        LocalTime operatingEndTime,
        Integer durationMinutes,
        Boolean reservationRequired
) {
}
