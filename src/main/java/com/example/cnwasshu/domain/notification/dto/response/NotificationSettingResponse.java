package com.example.cnwasshu.domain.notification.dto.response;

import com.example.cnwasshu.domain.notification.entity.NotificationSetting;

public record NotificationSettingResponse(
        boolean courseDayBefore,
        boolean reservationDayBefore,
        boolean reservation3hBefore,
        boolean reservation1hBefore,
        boolean reservation30mBefore,
        boolean surveyEnabled
) {

    public static NotificationSettingResponse from(NotificationSetting setting) {
        return new NotificationSettingResponse(
                setting.isCourseDayBefore(),
                setting.isReservationDayBefore(),
                setting.isReservation3hBefore(),
                setting.isReservation1hBefore(),
                setting.isReservation30mBefore(),
                setting.isSurveyEnabled()
        );
    }
}
