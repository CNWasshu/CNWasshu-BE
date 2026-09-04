package com.example.cnwasshu.domain.course.dto.response;

import com.example.cnwasshu.domain.course.entity.CourseItem;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.Comparator;

public record CourseItemResponse(
        Long id,
        Long activityId,
        Long restaurantId,
        Long reservationId,
        String title,
        Integer dayNo,
        LocalTime startTime,
        LocalTime endTime,
        Integer sortOrder,
        String address,
        BigDecimal latitude,
        BigDecimal longitude,
        String memo,
        Integer distanceMeters,
        Integer travelTimeSeconds
) {
    public static final Comparator<CourseItemResponse> COURSE_ORDER = Comparator
            .comparing(CourseItemResponse::dayNo)
            .thenComparing(CourseItemResponse::sortOrder)
            .thenComparing(CourseItemResponse::startTime);

    public static CourseItemResponse from(CourseItem item) {
        return new CourseItemResponse(
                item.getId(),
                item.getActivityId(),
                item.getRestaurantId(),
                item.getReservationId(),
                item.getTitle(),
                item.getDayNo(),
                item.getStartTime(),
                item.getEndTime(),
                item.getSortOrder(),
                item.getAddress(),
                item.getLatitude(),
                item.getLongitude(),
                item.getMemo(),
                null,
                null
        );
    }

    public CourseItemResponse withRouteToNext(Integer distanceMeters, Integer travelTimeSeconds) {
        return new CourseItemResponse(
                id, activityId, restaurantId, reservationId, title, dayNo, startTime, endTime, sortOrder,
                address, latitude, longitude, memo, distanceMeters, travelTimeSeconds
        );
    }
}
