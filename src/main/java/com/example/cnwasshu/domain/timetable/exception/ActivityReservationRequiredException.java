package com.example.cnwasshu.domain.timetable.exception;

import com.example.cnwasshu.common.exception.BusinessException;
import com.example.cnwasshu.common.exception.ErrorCode;

public class ActivityReservationRequiredException extends BusinessException {

    public ActivityReservationRequiredException(Long activityId) {
        super(
                ErrorCode.ACTIVITY_RESERVATION_REQUIRED,
                "예약 완료 후 추가할 수 있는 체험입니다. activityId=" + activityId
        );
    }
}
