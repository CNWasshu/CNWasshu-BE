package com.example.cnwasshu.domain.course.exception;

public class CourseNotFoundException extends RuntimeException {
    public CourseNotFoundException(Long courseId) {
        super("코스를 찾을 수 없습니다. courseId=" + courseId);
    }
}