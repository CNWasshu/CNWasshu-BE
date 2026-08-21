package com.example.cnwasshu.domain.notification.entity;

import com.example.cnwasshu.common.entity.BaseTimeEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "notification_setting")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NotificationSetting extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notification_setting_id")
    private Long id;

    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    @Column(name = "course_day_before", nullable = false)
    private boolean courseDayBefore;

    @Column(name = "reservation_day_before", nullable = false)
    private boolean reservationDayBefore;

    @Column(name = "reservation_3h_before", nullable = false)
    private boolean reservation3hBefore;

    @Column(name = "reservation_1h_before", nullable = false)
    private boolean reservation1hBefore;

    @Column(name = "reservation_30m_before", nullable = false)
    private boolean reservation30mBefore;

    private NotificationSetting(Long userId, boolean courseDayBefore, boolean reservationDayBefore,
                                 boolean reservation3hBefore, boolean reservation1hBefore,
                                 boolean reservation30mBefore) {
        this.userId = userId;
        this.courseDayBefore = courseDayBefore;
        this.reservationDayBefore = reservationDayBefore;
        this.reservation3hBefore = reservation3hBefore;
        this.reservation1hBefore = reservation1hBefore;
        this.reservation30mBefore = reservation30mBefore;
    }

    public static NotificationSetting createDefault(Long userId) {
        return new NotificationSetting(userId, true, true, true, true, true);
    }

    public void update(boolean courseDayBefore, boolean reservationDayBefore,
                        boolean reservation3hBefore, boolean reservation1hBefore,
                        boolean reservation30mBefore) {
        this.courseDayBefore = courseDayBefore;
        this.reservationDayBefore = reservationDayBefore;
        this.reservation3hBefore = reservation3hBefore;
        this.reservation1hBefore = reservation1hBefore;
        this.reservation30mBefore = reservation30mBefore;
    }
}
