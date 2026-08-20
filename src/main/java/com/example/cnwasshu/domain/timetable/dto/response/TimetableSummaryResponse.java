package com.example.cnwasshu.domain.timetable.dto.response;

import com.example.cnwasshu.domain.course.entity.Course;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record TimetableSummaryResponse(
        Long timetableId,
        String timetableName,
        LocalDate startDate,
        LocalDate endDate,
        Integer scheduleCount,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    public static TimetableSummaryResponse from(Course course) {
        return new TimetableSummaryResponse(
                course.getId(),
                course.getCourseName(),
                course.getStartDate(),
                course.getEndDate(),
                course.getItems().size(),
                course.getCreatedAt(),
                course.getUpdatedAt()
        );
    }
}
