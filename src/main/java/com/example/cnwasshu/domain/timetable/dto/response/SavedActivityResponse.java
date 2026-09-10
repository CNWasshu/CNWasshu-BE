package com.example.cnwasshu.domain.timetable.dto.response;

import com.example.cnwasshu.domain.timetable.entity.ActivityOperatingType;
import com.example.cnwasshu.domain.timetable.service.SavedActivityInfo;

import java.time.LocalTime;

public record SavedActivityResponse(
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

    public static SavedActivityResponse from(SavedActivityInfo activity) {
        return new SavedActivityResponse(
                activity.activityId(),
                activity.title(),
                activity.thumbnailUrl(),
                activity.region(),
                activity.operatingType(),
                activity.operatingStartTime(),
                activity.operatingEndTime(),
                activity.durationMinutes(),
                activity.reservationRequired()
        );
    }
}
