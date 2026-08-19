package com.example.cnwasshu.domain.course.exception;

public class InvalidTravelPeriodException extends RuntimeException {
    public InvalidTravelPeriodException() {
        super("여행 종료일은 시작일보다 빠를 수 없습니다.");
    }
}
