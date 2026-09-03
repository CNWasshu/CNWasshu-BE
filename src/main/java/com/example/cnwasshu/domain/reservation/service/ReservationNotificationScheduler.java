package com.example.cnwasshu.domain.reservation.service;

import com.example.cnwasshu.domain.notification.entity.ReservationReminderType;
import com.example.cnwasshu.domain.notification.service.NotificationService;
import com.example.cnwasshu.domain.reservation.entity.Reservation;
import com.example.cnwasshu.domain.reservation.entity.ReservationStatus;
import com.example.cnwasshu.domain.reservation.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ReservationNotificationScheduler {

    private static final ZoneId KOREA_ZONE_ID =
            ZoneId.of("Asia/Seoul");

    private final ReservationRepository reservationRepository;
    private final NotificationService notificationService;

    @Scheduled(
            cron = "0 * * * * *",
            zone = "Asia/Seoul"
    )
    @Transactional
    public void processReservationReminders() {

        LocalDateTime now =
                LocalDateTime.now(KOREA_ZONE_ID)
                        .truncatedTo(
                                ChronoUnit.MINUTES
                        );

        LocalDate today =
                now.toLocalDate();

        LocalDate tomorrow =
                today.plusDays(1);

        List<Reservation> reservations =
                reservationRepository
                        .findByReservationDateBetweenAndStatusAndDeletedAtIsNullOrderByReservationDateAscReservationTimeAsc(
                                today,
                                tomorrow,
                                ReservationStatus.CONFIRMED
                        );

        reservations.forEach(
                reservation ->
                        processReservation(
                                reservation,
                                now
                        )
        );
    }

    void processReservation(
            Reservation reservation,
            LocalDateTime now
    ) {

        LocalDateTime reservationDateTime =
                LocalDateTime.of(
                        reservation.getReservationDate(),
                        reservation.getReservationTime()
                ).truncatedTo(
                        ChronoUnit.MINUTES
                );

        long minutesUntilReservation =
                ChronoUnit.MINUTES.between(
                        now,
                        reservationDateTime
                );

        ReservationReminderType
                .fromMinutesBefore(
                        minutesUntilReservation
                )
                .ifPresent(reminderType ->
                        notificationService
                                .createReservationReminderNotification(
                                        reservation
                                                .getUser()
                                                .getId(),
                                        reservation
                                                .getId(),
                                        reservation
                                                .getActivity()
                                                .getTitle(),
                                        reminderType
                                )
                );
    }
}