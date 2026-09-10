package com.example.cnwasshu.domain.notification.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.Optional;

@Getter
@RequiredArgsConstructor
public enum ReservationReminderType {

    DAY_BEFORE(
            24 * 60,
            "예약 하루 전 알림"
    ),

    THREE_HOURS_BEFORE(
            3 * 60,
            "예약 3시간 전 알림"
    ),

    ONE_HOUR_BEFORE(
            60,
            "예약 1시간 전 알림"
    ),

    THIRTY_MINUTES_BEFORE(
            30,
            "예약 30분 전 알림"
    );

    private final long minutesBefore;
    private final String title;

    public static Optional<ReservationReminderType> fromMinutesBefore(
            long minutesBefore
    ) {
        return Arrays.stream(values())
                .filter(type ->
                        type.minutesBefore == minutesBefore
                )
                .findFirst();
    }

    public String createContent(
            String activityTitle
    ) {
        return switch (this) {
            case DAY_BEFORE ->
                    "내일 " + activityTitle
                            + " 예약이 예정되어 있어요.";

            case THREE_HOURS_BEFORE ->
                    activityTitle
                            + " 예약까지 3시간 남았어요.";

            case ONE_HOUR_BEFORE ->
                    activityTitle
                            + " 예약까지 1시간 남았어요.";

            case THIRTY_MINUTES_BEFORE ->
                    activityTitle
                            + " 예약까지 30분 남았어요.";
        };
    }
}