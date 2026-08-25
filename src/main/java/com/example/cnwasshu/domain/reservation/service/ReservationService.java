package com.example.cnwasshu.domain.reservation.service;

import com.example.cnwasshu.domain.home.entity.Activity;
import com.example.cnwasshu.domain.home.entity.ActivityStatus;
import com.example.cnwasshu.domain.home.repository.ActivityRepository;
import com.example.cnwasshu.domain.reservation.dto.AvailableTimeResponse;
import com.example.cnwasshu.domain.reservation.dto.ReservationCreateRequest;
import com.example.cnwasshu.domain.reservation.dto.ReservationResponse;
import com.example.cnwasshu.domain.reservation.entity.Reservation;
import com.example.cnwasshu.domain.reservation.entity.ReservationStatus;
import com.example.cnwasshu.domain.reservation.repository.ReservationRepository;
import com.example.cnwasshu.domain.user.entity.User;
import com.example.cnwasshu.domain.user.repository.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReservationService {

    private static final int SLOT_INTERVAL_HOURS = 1;

    private final ReservationRepository reservationRepository;
    private final ActivityRepository activityRepository;
    private final UserRepository userRepository;
    private final EntityManager entityManager;

    @Transactional
    public ReservationResponse createReservation(
            Long userId,
            ReservationCreateRequest request
    ) {
        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new IllegalArgumentException("사용자를 찾을 수 없습니다.")
                );

        Activity activity = activityRepository.findByIdAndDeletedAtIsNull(request.activityId())
                .orElseThrow(() ->
                        new IllegalArgumentException("체험을 찾을 수 없습니다.")
                );

        entityManager.lock(activity, LockModeType.PESSIMISTIC_WRITE);

        validateActivityForReservation(activity);
        validateReservationDate(activity, request.reservationDate());

        validateDuplicateReservation(
                userId,
                activity.getId(),
                request.reservationDate()
        );

        validatePeopleCount(activity, request.peopleCount());

        validateReservationTime(
                activity,
                request.reservationDate(),
                request.reservationTime()
        );

        validateNoTimeConflict(
                activity,
                request.reservationDate(),
                request.reservationTime(),
                null
        );

        Reservation reservation = Reservation.create(
                user,
                activity,
                request.reservationDate(),
                request.reservationTime(),
                request.peopleCount(),
                request.withChild()
        );

        Reservation savedReservation =
                reservationRepository.save(reservation);

        return ReservationResponse.from(savedReservation);
    }

    public List<AvailableTimeResponse> getAvailableTimes(
            Long activityId,
            LocalDate date
    ) {
        Activity activity = activityRepository.findByIdAndDeletedAtIsNull(activityId)
                .orElseThrow(() ->
                        new IllegalArgumentException("체험을 찾을 수 없습니다.")
                );

        validateActivityForReservation(activity);

        LocalTime operatingStartTime =
                activity.getOperatingStartTime();

        LocalTime operatingEndTime =
                activity.getOperatingEndTime();

        Integer duration =
                activity.getDuration();

        if (!isDateAvailable(activity, date)) {
            return List.of();
        }

        List<Reservation> reservations =
                reservationRepository
                        .findByActivityIdAndReservationDateAndStatusAndDeletedAtIsNull(
                                activityId,
                                date,
                                ReservationStatus.CONFIRMED
                        );

        List<AvailableTimeResponse> result =
                new ArrayList<>();

        LocalTime slotTime = operatingStartTime;

        while (true) {

            LocalTime slotEndTime =
                    slotTime.plusMinutes(duration);

            if (slotEndTime.isAfter(operatingEndTime)) {
                break;
            }

            boolean available =
                    isTimeAvailable(
                            activity,
                            date,
                            slotTime,
                            reservations,
                            null
                    );

            result.add(
                    new AvailableTimeResponse(
                            slotTime,
                            available
                    )
            );

            slotTime =
                    slotTime.plusHours(SLOT_INTERVAL_HOURS);
        }

        return result;
    }

    public List<ReservationResponse> getReservations(
            Long userId
    ) {
        return reservationRepository
                .findByUserIdAndStatusAndDeletedAtIsNullOrderByReservationDateDescReservationTimeDesc(
                        userId,
                        ReservationStatus.CONFIRMED
                )
                .stream()
                .map(ReservationResponse::from)
                .toList();
    }

    public List<ReservationResponse> getReservationsByDate(
            Long userId,
            LocalDate date
    ) {
        return reservationRepository
                .findByUserIdAndReservationDateAndStatusAndDeletedAtIsNullOrderByReservationTimeAsc(
                        userId,
                        date,
                        ReservationStatus.CONFIRMED
                )
                .stream()
                .map(ReservationResponse::from)
                .toList();
    }

    public List<ReservationResponse> getReservationsByPeriod(
            Long userId,
            LocalDate startDate,
            LocalDate endDate
    ) {
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException(
                    "시작 날짜는 종료 날짜보다 늦을 수 없습니다."
            );
        }

        return reservationRepository
                .findByUserIdAndReservationDateBetweenAndStatusAndDeletedAtIsNullOrderByReservationDateAscReservationTimeAsc(
                        userId,
                        startDate,
                        endDate,
                        ReservationStatus.CONFIRMED
                )
                .stream()
                .map(ReservationResponse::from)
                .toList();
    }

    private void validateActivityForReservation(Activity activity) {

        if (!Boolean.TRUE.equals(activity.getReservationRequired())) {
            throw new IllegalArgumentException(
                    "예약이 필요한 체험이 아닙니다."
            );
        }

        if (activity.getStatus() != ActivityStatus.OPEN) {
            throw new IllegalArgumentException(
                    "현재 예약할 수 없는 체험입니다."
            );
        }

        if (activity.getOperatingStartTime() == null
                || activity.getOperatingEndTime() == null) {

            throw new IllegalArgumentException(
                    "체험 운영시간 정보가 없습니다."
            );
        }

        if (activity.getDuration() == null
                || activity.getDuration() <= 0) {

            throw new IllegalArgumentException(
                    "체험 소요시간 정보가 없습니다."
            );
        }

        LocalTime calculatedEndTime =
                activity.getOperatingStartTime()
                        .plusMinutes(activity.getDuration());

        if (calculatedEndTime.isAfter(
                activity.getOperatingEndTime()
        )) {
            throw new IllegalArgumentException(
                    "체험 소요시간이 운영시간보다 깁니다."
            );
        }
    }

    private void validateReservationDate(
            Activity activity,
            LocalDate reservationDate
    ) {

        LocalDate today = LocalDate.now();

        if (reservationDate.isBefore(today)) {
            throw new IllegalArgumentException(
                    "지난 날짜에는 예약할 수 없습니다."
            );
        }

        if (reservationDate.equals(today)
                && Boolean.FALSE.equals(
                activity.getTodayAvailable()
        )) {

            throw new IllegalArgumentException(
                    "당일 예약이 불가능한 체험입니다."
            );
        }

        if (activity.getStartDate() != null
                && reservationDate.isBefore(
                activity.getStartDate()
        )) {

            throw new IllegalArgumentException(
                    "체험 운영 기간이 아닙니다."
            );
        }

        if (activity.getEndDate() != null
                && reservationDate.isAfter(
                activity.getEndDate()
        )) {

            throw new IllegalArgumentException(
                    "체험 운영 기간이 아닙니다."
            );
        }
    }

    private void validatePeopleCount(
            Activity activity,
            Integer peopleCount
    ) {

        if (peopleCount == null || peopleCount < 1) {
            throw new IllegalArgumentException(
                    "예약 인원은 1명 이상이어야 합니다."
            );
        }

        Integer maxParticipants =
                activity.getMaxParticipants();

        if (maxParticipants != null
                && peopleCount > maxParticipants) {

            throw new IllegalArgumentException(
                    "최대 예약 가능 인원은 "
                            + maxParticipants
                            + "명입니다."
            );
        }
    }

    private void validateReservationTime(
            Activity activity,
            LocalDate reservationDate,
            LocalTime reservationTime
    ) {

        LocalTime operatingStartTime =
                activity.getOperatingStartTime();

        LocalTime operatingEndTime =
                activity.getOperatingEndTime();

        Integer duration =
                activity.getDuration();

        if (reservationTime.isBefore(
                operatingStartTime
        )) {
            throw new IllegalArgumentException(
                    "운영 시작시간 이전에는 예약할 수 없습니다."
            );
        }

        if (!isValidSlotTime(
                operatingStartTime,
                reservationTime
        )) {
            throw new IllegalArgumentException(
                    "예약 가능한 시간이 아닙니다."
            );
        }

        LocalTime reservationEndTime =
                reservationTime.plusMinutes(duration);

        if (reservationEndTime.isAfter(
                operatingEndTime
        )) {
            throw new IllegalArgumentException(
                    "체험 운영시간을 초과하는 예약입니다."
            );
        }

        LocalDate today = LocalDate.now();

        if (reservationDate.equals(today)) {

            LocalTime now = LocalTime.now();

            if (!reservationTime.isAfter(now)) {
                throw new IllegalArgumentException(
                        "이미 지난 시간에는 예약할 수 없습니다."
                );
            }
        }
    }

    private void validateDuplicateReservation(
            Long userId,
            Long activityId,
            LocalDate reservationDate
    ) {
        boolean exists =
                reservationRepository
                        .existsByUserIdAndActivityIdAndReservationDateAndStatusAndDeletedAtIsNull(
                                userId,
                                activityId,
                                reservationDate,
                                ReservationStatus.CONFIRMED
                        );

        if (exists) {
            throw new IllegalArgumentException(
                    "이미 해당 날짜에 예약한 체험입니다."
            );
        }
    }

    private boolean isValidSlotTime(
            LocalTime operatingStartTime,
            LocalTime reservationTime
    ) {

        if (reservationTime.isBefore(
                operatingStartTime
        )) {
            return false;
        }

        long startMinutes =
                operatingStartTime.getHour() * 60L
                        + operatingStartTime.getMinute();

        long reservationMinutes =
                reservationTime.getHour() * 60L
                        + reservationTime.getMinute();

        long difference =
                reservationMinutes - startMinutes;

        return difference % 60 == 0;
    }

    private void validateNoTimeConflict(
            Activity activity,
            LocalDate reservationDate,
            LocalTime reservationTime,
            Long excludeReservationId
    ) {

        List<Reservation> reservations =
                reservationRepository
                        .findByActivityIdAndReservationDateAndStatusAndDeletedAtIsNull(
                                activity.getId(),
                                reservationDate,
                                ReservationStatus.CONFIRMED
                        );

        boolean available =
                isTimeAvailable(
                        activity,
                        reservationDate,
                        reservationTime,
                        reservations,
                        excludeReservationId
                );

        if (!available) {
            throw new IllegalArgumentException(
                    "이미 예약된 시간과 겹칩니다."
            );
        }
    }

    private boolean isTimeAvailable(
            Activity activity,
            LocalDate date,
            LocalTime newStartTime,
            List<Reservation> reservations,
            Long excludeReservationId
    ) {
        LocalDate today = LocalDate.now();

        if (date.equals(today)) {

            LocalTime now =
                    LocalTime.now();

            if (!newStartTime.isAfter(now)) {
                return false;
            }
        }

        Integer duration =
                activity.getDuration();

        LocalTime newEndTime =
                newStartTime.plusMinutes(duration);

        for (Reservation reservation : reservations) {

            if (excludeReservationId != null
                    && reservation.getId()
                    .equals(excludeReservationId)) {
                continue;
            }

            LocalTime existingStartTime =
                    reservation.getReservationTime();

            LocalTime existingEndTime =
                    existingStartTime.plusMinutes(duration);

            boolean overlap =
                    newStartTime.isBefore(existingEndTime)
                            && newEndTime.isAfter(
                            existingStartTime
                    );

            if (overlap) {
                return false;
            }
        }

        return true;
    }

    private boolean isDateAvailable(
            Activity activity,
            LocalDate date
    ) {

        LocalDate today =
                LocalDate.now();

        if (date.isBefore(today)) {
            return false;
        }

        if (date.equals(today)
                && Boolean.FALSE.equals(
                activity.getTodayAvailable()
        )) {
            return false;
        }

        if (activity.getStartDate() != null
                && date.isBefore(
                activity.getStartDate()
        )) {
            return false;
        }

        if (activity.getEndDate() != null
                && date.isAfter(
                activity.getEndDate()
        )) {
            return false;
        }

        return true;
    }
}