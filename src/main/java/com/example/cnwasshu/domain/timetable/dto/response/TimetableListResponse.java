package com.example.cnwasshu.domain.timetable.dto.response;

import com.example.cnwasshu.domain.course.entity.Course;

import java.util.List;

public record TimetableListResponse(
        List<TimetableSummaryResponse> items
) {

    public static TimetableListResponse from(List<Course> courses) {
        return new TimetableListResponse(
                courses.stream()
                        .map(TimetableSummaryResponse::from)
                        .toList()
        );
    }
}
