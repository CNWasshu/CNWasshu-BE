package com.example.cnwasshu.domain.course.dto.response;

import com.example.cnwasshu.domain.course.entity.Course;
import com.example.cnwasshu.domain.course.entity.CourseType;

import java.time.LocalDate;

/** 코스 목록/저장된 코스 불러오기 모달용 요약 정보 */
public record CourseSummaryResponse(
        Long id,
        String courseName,
        CourseType courseType,
        LocalDate startDate,
        LocalDate endDate,
        int itemCount
) {
    public static CourseSummaryResponse from(Course course) {
        return new CourseSummaryResponse(
                course.getId(),
                course.getCourseName(),
                course.getCourseType(),
                course.getStartDate(),
                course.getEndDate(),
                course.getItems().size()
        );
    }
}