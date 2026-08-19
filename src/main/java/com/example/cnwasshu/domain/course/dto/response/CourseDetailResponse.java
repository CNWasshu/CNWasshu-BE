package com.example.cnwasshu.domain.course.dto.response;

import com.example.cnwasshu.domain.course.entity.Course;
import com.example.cnwasshu.domain.course.entity.CourseType;

import java.time.LocalDate;
import java.util.List;

/** 코스 상세 화면(renderCourse) 응답 */
public record CourseDetailResponse(
        Long id,
        String courseName,
        CourseType courseType,
        Integer peopleCount,
        Boolean withChild,
        LocalDate startDate,
        LocalDate endDate,
        List<CourseItemResponse> items
) {
    public static CourseDetailResponse from(Course course) {
        return new CourseDetailResponse(
                course.getId(),
                course.getCourseName(),
                course.getCourseType(),
                course.getPeopleCount(),
                course.getWithChild(),
                course.getStartDate(),
                course.getEndDate(),
                course.getItems().stream()
                        .map(CourseItemResponse::from)
                        .sorted(CourseItemResponse.COURSE_ORDER)
                        .toList()
        );
    }
}
