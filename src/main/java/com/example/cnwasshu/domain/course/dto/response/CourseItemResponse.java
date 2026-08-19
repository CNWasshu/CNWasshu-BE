package com.example.cnwasshu.domain.course.dto.response;

import com.example.cnwasshu.domain.course.entity.CourseItem;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.Comparator;

public record CourseItemResponse(
        Long id,
        Long activityId,
        Long reservationId,
        String title,
        Integer dayNo,
        LocalTime startTime,
        LocalTime endTime,
        Integer sortOrder,
        String address,
        BigDecimal latitude,
        BigDecimal longitude,
        String memo
) {
    public static final Comparator<CourseItemResponse> COURSE_ORDER = Comparator
            .comparing(CourseItemResponse::dayNo)
            .thenComparing(CourseItemResponse::sortOrder)
            .thenComparing(CourseItemResponse::startTime);

    public static CourseItemResponse from(CourseItem item) {
        // TODO(activity 연동): activityId로 Activity를 조회하여 address/latitude/longitude를 채운다.
        // 직접 입력한 자유 일정(activityId == null)은 위치 값을 null로 유지한다.
        return new CourseItemResponse(
                item.getId(),
                item.getActivityId(),
                item.getReservationId(),
                item.getTitle(),
                item.getDayNo(),
                item.getStartTime(),
                item.getEndTime(),
                item.getSortOrder(),
                null,
                null,
                null,
                item.getMemo()
        );
    }
}
