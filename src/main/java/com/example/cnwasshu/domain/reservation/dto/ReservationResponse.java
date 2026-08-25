package com.example.cnwasshu.domain.reservation.dto;

import com.example.cnwasshu.domain.reservation.entity.Reservation;
import com.example.cnwasshu.domain.reservation.entity.ReservationStatus;

import java.time.LocalDate;
import java.time.LocalTime;

public record ReservationResponse(
        Long reservationId,
        Long activityId,
        String activityTitle,
        LocalDate reservationDate,
        LocalTime reservationTime,
        LocalTime endTime,
        Integer peopleCount,
        Boolean withChild,
        ReservationStatus status
) {

    public static ReservationResponse from(Reservation reservation) {

        Integer duration = reservation.getActivity().getDuration();

        LocalTime endTime = duration != null
                ? reservation.getReservationTime().plusMinutes(duration)
                : null;

        return new ReservationResponse(
                reservation.getId(),
                reservation.getActivity().getId(),
                reservation.getActivity().getTitle(),
                reservation.getReservationDate(),
                reservation.getReservationTime(),
                endTime,
                reservation.getPeopleCount(),
                reservation.getWithChild(),
                reservation.getStatus()
        );
    }
}