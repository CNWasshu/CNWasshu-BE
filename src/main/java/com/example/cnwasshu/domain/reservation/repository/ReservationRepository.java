package com.example.cnwasshu.domain.reservation.repository;

import com.example.cnwasshu.domain.reservation.entity.Reservation;
import com.example.cnwasshu.domain.reservation.entity.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    List<Reservation> findByActivityIdAndReservationDateAndStatusAndDeletedAtIsNull(
            Long activityId,
            LocalDate reservationDate,
            ReservationStatus status
    );

    List<Reservation> findByUserIdAndStatusAndDeletedAtIsNullOrderByReservationDateDescReservationTimeDesc(
            Long userId,
            ReservationStatus status
    );

    List<Reservation> findByUserIdAndReservationDateAndStatusAndDeletedAtIsNullOrderByReservationTimeAsc(
            Long userId,
            LocalDate reservationDate,
            ReservationStatus status
    );

    List<Reservation> findByUserIdAndReservationDateBetweenAndStatusAndDeletedAtIsNullOrderByReservationDateAscReservationTimeAsc(
            Long userId,
            LocalDate startDate,
            LocalDate endDate,
            ReservationStatus status
    );

    boolean existsByUserIdAndActivityIdAndReservationDateAndStatusAndDeletedAtIsNull(
            Long userId,
            Long activityId,
            LocalDate reservationDate,
            ReservationStatus status
    );

    Optional<Reservation> findByIdAndUserIdAndDeletedAtIsNull(
            Long reservationId,
            Long userId
    );
}