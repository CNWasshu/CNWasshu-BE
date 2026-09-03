package com.example.cnwasshu.domain.review.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Objects;

import org.springframework.stereotype.Component;

@Component
public class SurveyScheduleCalculator {

    private static final LocalTime QUIET_HOURS_START = LocalTime.of(21, 0);
    private static final LocalTime MORNING_SEND_TIME = LocalTime.of(10, 0);

    public LocalDateTime calculate(
            LocalDate courseDate,
            LocalTime startTime,
            LocalTime endTime
    ) {
        Objects.requireNonNull(courseDate, "코스 날짜가 필요합니다.");
        Objects.requireNonNull(startTime, "일정 시작 시간이 필요합니다.");
        Objects.requireNonNull(endTime, "일정 종료 시간이 필요합니다.");

        if (startTime.equals(endTime)) {
            throw new IllegalArgumentException("일정 시작 시간과 종료 시간은 같을 수 없습니다.");
        }

        LocalDateTime endDateTime = courseDate.atTime(endTime);
        if (endTime.isBefore(startTime)) {
            endDateTime = endDateTime.plusDays(1);
        }

        return adjustQuietHours(endDateTime.plusHours(1));
    }

    private LocalDateTime adjustQuietHours(LocalDateTime scheduledAt) {
        LocalTime scheduledTime = scheduledAt.toLocalTime();

        if (!scheduledTime.isBefore(QUIET_HOURS_START)) {
            return scheduledAt.toLocalDate().plusDays(1).atTime(MORNING_SEND_TIME);
        }
        if (scheduledTime.isBefore(MORNING_SEND_TIME)) {
            return scheduledAt.toLocalDate().atTime(MORNING_SEND_TIME);
        }
        return scheduledAt;
    }
}
