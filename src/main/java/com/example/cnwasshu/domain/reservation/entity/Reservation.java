package com.example.cnwasshu.domain.reservation.entity;

import com.example.cnwasshu.common.entity.BaseSoftDeleteEntity;
import com.example.cnwasshu.domain.home.entity.Activity;
import com.example.cnwasshu.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "reservation")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Reservation extends BaseSoftDeleteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reservation_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "activity_id", nullable = false)
    private Activity activity;

    @Column(name = "reservation_date", nullable = false)
    private LocalDate reservationDate;

    @Column(name = "reservation_time", nullable = false)
    private LocalTime reservationTime;

    @Column(name = "people_count", nullable = false)
    private Integer peopleCount;

    @Column(name = "with_child", nullable = false)
    private Boolean withChild;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ReservationStatus status;

    public static Reservation create(
            User user,
            Activity activity,
            LocalDate reservationDate,
            LocalTime reservationTime,
            Integer peopleCount,
            Boolean withChild
    ) {
        Reservation reservation = new Reservation();

        reservation.user = user;
        reservation.activity = activity;
        reservation.reservationDate = reservationDate;
        reservation.reservationTime = reservationTime;
        reservation.peopleCount = peopleCount;
        reservation.withChild = withChild;
        reservation.status = ReservationStatus.CONFIRMED;

        return reservation;
    }

    public void changeReservation(
            LocalDate reservationDate,
            LocalTime reservationTime,
            Integer peopleCount,
            Boolean withChild
    ) {
        this.reservationDate = reservationDate;
        this.reservationTime = reservationTime;
        this.peopleCount = peopleCount;
        this.withChild = withChild;
    }

    public void cancel() {
        this.status = ReservationStatus.CANCELLED;
    }
}