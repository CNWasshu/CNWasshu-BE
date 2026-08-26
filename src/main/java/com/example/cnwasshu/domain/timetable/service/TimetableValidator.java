package com.example.cnwasshu.domain.timetable.service;

import com.example.cnwasshu.domain.timetable.dto.request.TimetableDayRequest;
import com.example.cnwasshu.domain.timetable.dto.request.TimetableSaveRequest;
import com.example.cnwasshu.domain.timetable.dto.request.TimetableScheduleRequest;
import com.example.cnwasshu.domain.timetable.entity.ActivityOperatingType;
import com.example.cnwasshu.domain.timetable.entity.TimetableScheduleType;
import com.example.cnwasshu.domain.timetable.exception.ActivityOutsideOperatingHoursException;
import com.example.cnwasshu.domain.timetable.exception.ActivityReservationRequiredException;
import com.example.cnwasshu.domain.timetable.exception.InvalidActivityReservationException;
import com.example.cnwasshu.domain.timetable.exception.InvalidScheduleTimeException;
import com.example.cnwasshu.domain.timetable.exception.InvalidScheduleTypeException;
import com.example.cnwasshu.domain.timetable.exception.InvalidTimetableDayException;
import com.example.cnwasshu.domain.timetable.exception.InvalidTimetablePeriodException;
import com.example.cnwasshu.domain.timetable.exception.RestaurantOutsideOperatingHoursException;
import com.example.cnwasshu.domain.timetable.exception.ScheduleTimeConflictException;
import com.example.cnwasshu.domain.timetable.exception.TimetableActivityNotFoundException;
import com.example.cnwasshu.domain.timetable.exception.TimetableRestaurantNotFoundException;
import com.example.cnwasshu.domain.timetable.exception.TimetableScheduleRequiredException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class TimetableValidator {

    private static final int MAX_TRIP_DAY_COUNT = 7;
    private static final String CONFIRMED_RESERVATION_STATUS = "CONFIRMED";

    private final TimetableReferenceQueryPort referenceQueryPort;

    public void validate(Long userId, TimetableSaveRequest request) {
        int tripDayCount = validatePeriod(request.startDate(), request.endDate());
        validateDays(request.days(), request.startDate(), tripDayCount);
        validateScheduleExists(request.days());

        List<TimetableScheduleRequest> allSchedules = request.days().stream()
                .flatMap(day -> day.schedules().stream())
                .toList();
        allSchedules.forEach(this::validateScheduleType);

        Map<Long, TimetableActivityInfo> activities = referenceQueryPort.findActivities(
                collectActivityIds(allSchedules)
        );
        Map<Long, TimetableRestaurantInfo> restaurants = referenceQueryPort.findRestaurants(
                collectRestaurantIds(allSchedules)
        );
        Map<Long, TimetableReservationInfo> reservations = referenceQueryPort.findReservations(
                collectReservationIds(allSchedules)
        );

        request.days().forEach(day -> {
            day.schedules().forEach(schedule -> {
                validateScheduleTime(schedule);
                validateReferences(userId, day.date(), schedule, activities, restaurants, reservations);
            });
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

    private void validateScheduleType(TimetableScheduleRequest schedule) {
        boolean validFreeSchedule = schedule.scheduleType() == TimetableScheduleType.FREE
                && schedule.activityId() == null
                && schedule.restaurantId() == null
                && schedule.reservationId() == null;
        boolean validActivitySchedule = schedule.scheduleType() == TimetableScheduleType.ACTIVITY
                && isPositive(schedule.activityId())
                && schedule.restaurantId() == null
                && (schedule.reservationId() == null || isPositive(schedule.reservationId()));
        boolean validRestaurantSchedule = schedule.scheduleType() == TimetableScheduleType.RESTAURANT
                && schedule.activityId() == null
                && isPositive(schedule.restaurantId())
                && schedule.reservationId() == null;

        if (!validFreeSchedule && !validActivitySchedule && !validRestaurantSchedule) {
            throw new InvalidScheduleTypeException(schedule.clientScheduleId());
        }
    }

    private boolean isPositive(Long id) {
        return id != null && id > 0;
    }

    private Set<Long> collectActivityIds(List<TimetableScheduleRequest> schedules) {
        Set<Long> activityIds = new HashSet<>();
        schedules.stream()
                .map(TimetableScheduleRequest::activityId)
                .filter(id -> id != null)
                .forEach(activityIds::add);
        return activityIds;
    }

    private Set<Long> collectReservationIds(List<TimetableScheduleRequest> schedules) {
        Set<Long> reservationIds = new HashSet<>();
        schedules.stream()
                .map(TimetableScheduleRequest::reservationId)
                .filter(id -> id != null)
                .forEach(reservationIds::add);
        return reservationIds;
    }

    private Set<Long> collectRestaurantIds(List<TimetableScheduleRequest> schedules) {
        Set<Long> restaurantIds = new HashSet<>();
        schedules.stream()
                .map(TimetableScheduleRequest::restaurantId)
                .filter(id -> id != null)
                .forEach(restaurantIds::add);
        return restaurantIds;
    }

    private void validateScheduleTime(TimetableScheduleRequest schedule) {
        if (!schedule.startTime().isBefore(schedule.endTime())) {
            throw new InvalidScheduleTimeException(schedule.clientScheduleId());
        }
    }

    private void validateReferences(
            Long userId,
            LocalDate scheduleDate,
            TimetableScheduleRequest schedule,
            Map<Long, TimetableActivityInfo> activities,
            Map<Long, TimetableRestaurantInfo> restaurants,
            Map<Long, TimetableReservationInfo> reservations
    ) {
        if (schedule.scheduleType() == TimetableScheduleType.FREE) {
            return;
        }

        if (schedule.scheduleType() == TimetableScheduleType.RESTAURANT) {
            validateRestaurant(schedule, restaurants);
            return;
        }

        TimetableActivityInfo activity = activities.get(schedule.activityId());
        if (activity == null) {
            throw new TimetableActivityNotFoundException(schedule.activityId());
        }

        validateOperatingHours(schedule, activity);
        if (Boolean.TRUE.equals(activity.reservationRequired())) {
            validateRequiredReservation(userId, scheduleDate, schedule, activity, reservations);
        } else if (schedule.reservationId() != null) {
            throw new InvalidActivityReservationException(schedule.activityId(), schedule.reservationId());
        }
    }

    private void validateRestaurant(
            TimetableScheduleRequest schedule,
            Map<Long, TimetableRestaurantInfo> restaurants
    ) {
        TimetableRestaurantInfo restaurant = restaurants.get(schedule.restaurantId());
        if (restaurant == null) {
            throw new TimetableRestaurantNotFoundException(schedule.restaurantId());
        }

        if (restaurant.operatingType() == ActivityOperatingType.ALWAYS) {
            return;
        }

        boolean startsBeforeOpening = schedule.startTime().isBefore(restaurant.operatingStartTime());
        boolean endsAfterClosing = schedule.endTime().isAfter(restaurant.operatingEndTime());
        if (startsBeforeOpening || endsAfterClosing) {
            throw new RestaurantOutsideOperatingHoursException(restaurant.restaurantId());
        }
    }

    private void validateOperatingHours(
            TimetableScheduleRequest schedule,
            TimetableActivityInfo activity
    ) {
        if (activity.operatingType() == ActivityOperatingType.ALWAYS) {
            return;
        }

        boolean startsBeforeOpening = schedule.startTime().isBefore(activity.operatingStartTime());
        boolean endsAfterClosing = schedule.endTime().isAfter(activity.operatingEndTime());
        if (startsBeforeOpening || endsAfterClosing) {
            throw new ActivityOutsideOperatingHoursException(activity.activityId());
        }
    }

    private void validateRequiredReservation(
            Long userId,
            LocalDate scheduleDate,
            TimetableScheduleRequest schedule,
            TimetableActivityInfo activity,
            Map<Long, TimetableReservationInfo> reservations
    ) {
        if (schedule.reservationId() == null) {
            throw new ActivityReservationRequiredException(activity.activityId());
        }

        TimetableReservationInfo reservation = reservations.get(schedule.reservationId());
        boolean invalidReservation = reservation == null
                || !userId.equals(reservation.userId())
                || !activity.activityId().equals(reservation.activityId())
                || !scheduleDate.equals(reservation.reservationDate())
                || !CONFIRMED_RESERVATION_STATUS.equalsIgnoreCase(reservation.status())
                || !schedule.startTime().equals(reservation.reservationTime())
                || !matchesReservationEndTime(schedule.endTime(), reservation, activity);

        if (invalidReservation) {
            throw new InvalidActivityReservationException(activity.activityId(), schedule.reservationId());
        }
    }

    private boolean matchesReservationEndTime(
            LocalTime scheduleEndTime,
            TimetableReservationInfo reservation,
            TimetableActivityInfo activity
    ) {
        if (activity.durationMinutes() == null) {
            return true;
        }
        return scheduleEndTime.equals(reservation.reservationTime().plusMinutes(activity.durationMinutes()));
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
