package com.example.cnwasshu.domain.timetable.dto.response;

import com.example.cnwasshu.domain.course.entity.CourseItem;

import java.time.LocalDate;
import java.util.List;

public record TimetableDayResponse(
        Integer dayNo,
        LocalDate date,
        List<TimetableScheduleResponse> schedules
) {

    public static TimetableDayResponse from(
            LocalDate timetableStartDate,
            Integer dayNo,
            List<CourseItem> items
    ) {
        List<TimetableScheduleResponse> schedules = items.stream()
                .filter(item -> dayNo.equals(item.getDayNo()))
                .map(TimetableScheduleResponse::from)
                .sorted(TimetableScheduleResponse.SCHEDULE_ORDER)
                .toList();

        return new TimetableDayResponse(
                dayNo,
                timetableStartDate.plusDays(dayNo - 1L),
                schedules
        );
    }
}
