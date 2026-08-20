package com.example.cnwasshu.domain.timetable.service;

import com.example.cnwasshu.domain.timetable.dto.request.TimetableDayRequest;
import com.example.cnwasshu.domain.timetable.dto.request.TimetableSaveRequest;
import com.example.cnwasshu.domain.timetable.dto.request.TimetableScheduleRequest;
import com.example.cnwasshu.domain.timetable.entity.TimetableScheduleType;
import com.example.cnwasshu.domain.timetable.exception.InvalidScheduleTimeException;
import com.example.cnwasshu.domain.timetable.exception.InvalidScheduleTypeException;
import com.example.cnwasshu.domain.timetable.exception.InvalidTimetableDayException;
import com.example.cnwasshu.domain.timetable.exception.InvalidTimetablePeriodException;
import com.example.cnwasshu.domain.timetable.exception.ScheduleTimeConflictException;
import com.example.cnwasshu.domain.timetable.exception.TimetableScheduleRequiredException;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class TimetableValidator {

    private static final int MAX_TRIP_DAY_COUNT = 7;

    public void validate(TimetableSaveRequest request) {
        int tripDayCount = validatePeriod(request.startDate(), request.endDate());
        validateDays(request.days(), request.startDate(), tripDayCount);
        validateScheduleExists(request.days());

        request.days().forEach(day -> {
            validateSchedules(day.schedules());
            validateNoOverlap(day.dayNo(), day.schedules());
        });
    }

    private int validatePeriod(LocalDate startDate, LocalDate endDate) {
        long dayCount = ChronoUnit.DAYS.between(startDate, endDate) + 1;

        if (dayCount < 1 || dayCount > MAX_TRIP_DAY_COUNT) {
            throw new InvalidTimetablePeriodException(
                    "여행 기간은 시작일과 종료일을 포함해 1일 이상 7일 이하여야 합니다."
            );
        }

        return Math.toIntExact(dayCount);
    }

    private void validateDays(List<TimetableDayRequest> days, LocalDate startDate, int tripDayCount) {
        if (days.size() != tripDayCount) {
            throw new InvalidTimetableDayException("여행 기간의 모든 날짜를 한 번씩 포함해야 합니다.");
        }

        Set<Integer> dayNumbers = new HashSet<>();
        Set<LocalDate> dates = new HashSet<>();

        for (TimetableDayRequest day : days) {
            LocalDate expectedDate = startDate.plusDays(day.dayNo() - 1L);
            boolean duplicated = !dayNumbers.add(day.dayNo()) || !dates.add(day.date());
            boolean outsidePeriod = day.dayNo() < 1 || day.dayNo() > tripDayCount;

            if (duplicated || outsidePeriod || !expectedDate.equals(day.date())) {
                throw new InvalidTimetableDayException(
                        "dayNo와 date는 여행 시작일부터 순서대로 일치해야 합니다. dayNo=" + day.dayNo()
                );
            }
        }
    }

    private void validateScheduleExists(List<TimetableDayRequest> days) {
        boolean hasSchedule = days.stream().anyMatch(day -> !day.schedules().isEmpty());
        if (!hasSchedule) {
            throw new TimetableScheduleRequiredException();
        }
    }

    private void validateSchedules(List<TimetableScheduleRequest> schedules) {
        schedules.forEach(schedule -> {
            validateScheduleType(schedule);
            validateScheduleTime(schedule);
        });
    }

    private void validateScheduleType(TimetableScheduleRequest schedule) {
        boolean validFreeSchedule = schedule.scheduleType() == TimetableScheduleType.FREE
                && schedule.activityId() == null
                && schedule.reservationId() == null;
        boolean validActivitySchedule = schedule.scheduleType() == TimetableScheduleType.ACTIVITY
                && isPositive(schedule.activityId())
                && (schedule.reservationId() == null || isPositive(schedule.reservationId()));

        if (!validFreeSchedule && !validActivitySchedule) {
            throw new InvalidScheduleTypeException(schedule.clientScheduleId());
        }
    }

    private boolean isPositive(Long id) {
        return id != null && id > 0;
    }

    private void validateScheduleTime(TimetableScheduleRequest schedule) {
        if (!schedule.startTime().isBefore(schedule.endTime())) {
            throw new InvalidScheduleTimeException(schedule.clientScheduleId());
        }
    }

    private void validateNoOverlap(Integer dayNo, List<TimetableScheduleRequest> schedules) {
        List<TimetableScheduleRequest> sortedSchedules = schedules.stream()
                .sorted(Comparator.comparing(TimetableScheduleRequest::startTime))
                .toList();

        for (int index = 1; index < sortedSchedules.size(); index++) {
            TimetableScheduleRequest previous = sortedSchedules.get(index - 1);
            TimetableScheduleRequest current = sortedSchedules.get(index);

            if (current.startTime().isBefore(previous.endTime())) {
                throw new ScheduleTimeConflictException(
                        dayNo,
                        previous.clientScheduleId(),
                        current.clientScheduleId()
                );
            }
        }
    }
}
