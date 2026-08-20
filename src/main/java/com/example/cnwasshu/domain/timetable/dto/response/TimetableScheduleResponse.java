package com.example.cnwasshu.domain.timetable.dto.response;

import com.example.cnwasshu.domain.course.entity.CourseItem;
import com.example.cnwasshu.domain.timetable.entity.TimetableScheduleType;

import java.time.LocalTime;
import java.util.Comparator;

public record TimetableScheduleResponse(
        Long scheduleId,
        TimetableScheduleType scheduleType,
        Long activityId,
        Long reservationId,
        String title,
        LocalTime startTime,
        LocalTime endTime,
        String memo,
        Integer sortOrder
) {

    public static final Comparator<TimetableScheduleResponse> SCHEDULE_ORDER = Comparator
            .comparing(TimetableScheduleResponse::sortOrder)
            .thenComparing(TimetableScheduleResponse::startTime)
            .thenComparing(TimetableScheduleResponse::scheduleId);

    public static TimetableScheduleResponse from(CourseItem item) {
        TimetableScheduleType scheduleType = item.getActivityId() == null
                ? TimetableScheduleType.FREE
                : TimetableScheduleType.ACTIVITY;

        return new TimetableScheduleResponse(
                item.getId(),
                scheduleType,
                item.getActivityId(),
                item.getReservationId(),
                item.getTitle(),
                item.getStartTime(),
                item.getEndTime(),
                item.getMemo(),
                item.getSortOrder()
        );
    }
}
