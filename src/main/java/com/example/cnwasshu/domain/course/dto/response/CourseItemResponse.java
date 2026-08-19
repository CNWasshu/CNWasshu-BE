package com.example.cnwasshu.domain.course.dto.response;

import com.example.cnwasshu.domain.course.entity.CourseItem;

import java.time.LocalTime;

public record CourseItemResponse(
        Long id,
        Long activityId,
        Long reservationId,
        String title,
        Integer dayNo,
        LocalTime startTime,
        LocalTime endTime,
        String memo,
        Integer sortOrder
) {
    public static CourseItemResponse from(CourseItem item) {
        return new CourseItemResponse(
                item.getId(),
                item.getActivityId(),
                item.getReservationId(),
                item.getTitle(),
                item.getDayNo(),
                item.getStartTime(),
                item.getEndTime(),
                item.getMemo(),
                item.getSortOrder()
        );
    }
}