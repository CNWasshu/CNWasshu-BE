package com.example.cnwasshu.domain.course.exception;

public class CourseTimeOverlapException extends RuntimeException {
    public CourseTimeOverlapException(int dayNo) {
        super(dayNo + "일차에 시간이 겹치는 일정이 있습니다.");
    }
}