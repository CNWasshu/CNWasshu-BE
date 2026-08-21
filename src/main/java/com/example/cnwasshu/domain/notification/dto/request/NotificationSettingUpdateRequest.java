package com.example.cnwasshu.domain.notification.dto.request;

public record NotificationSettingUpdateRequest(
        Boolean courseDayBefore,
        Boolean reservationDayBefore,
        Boolean reservation3hBefore,
        Boolean reservation1hBefore,
        Boolean reservation30mBefore
) {
}
