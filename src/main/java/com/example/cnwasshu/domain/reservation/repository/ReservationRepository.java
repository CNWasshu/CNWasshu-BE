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

    List<Reservation> findByUserIdAndDeletedAtIsNullOrderByReservationDateDescReservationTimeDesc(
            Long userId
    );

    Optional<Reservation> findByIdAndUserIdAndDeletedAtIsNull(
            Long reservationId,
            Long userId
    );
}