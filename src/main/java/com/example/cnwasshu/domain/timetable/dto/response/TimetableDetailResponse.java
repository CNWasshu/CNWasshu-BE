package com.example.cnwasshu.domain.timetable.dto.response;

import com.example.cnwasshu.domain.course.entity.Course;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.IntStream;

import static java.time.temporal.ChronoUnit.DAYS;

public record TimetableDetailResponse(
        Long timetableId,
        String timetableName,
        Integer peopleCount,
        Boolean withChild,
        LocalDate startDate,
        LocalDate endDate,
        List<TimetableDayResponse> days
) {

    public static TimetableDetailResponse from(Course course) {
        int dayCount = Math.toIntExact(DAYS.between(course.getStartDate(), course.getEndDate()) + 1);
        List<TimetableDayResponse> days = IntStream.rangeClosed(1, dayCount)
                .mapToObj(dayNo -> TimetableDayResponse.from(
                        course.getStartDate(),
                        dayNo,
                        course.getItems()
                ))
                .toList();

        return new TimetableDetailResponse(
                course.getId(),
                course.getCourseName(),
                course.getPeopleCount(),
                course.getWithChild(),
                course.getStartDate(),
                course.getEndDate(),
                days
        );
    }
}
