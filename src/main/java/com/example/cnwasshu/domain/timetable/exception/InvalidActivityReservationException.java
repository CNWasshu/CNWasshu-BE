package com.example.cnwasshu.domain.timetable.exception;

import com.example.cnwasshu.common.exception.BusinessException;
import com.example.cnwasshu.common.exception.ErrorCode;

public class InvalidActivityReservationException extends BusinessException {

    public InvalidActivityReservationException(Long activityId, Long reservationId) {
        super(
                ErrorCode.INVALID_ACTIVITY_RESERVATION,
                "체험과 예약 정보가 일치하지 않습니다. activityId=" + activityId
                        + ", reservationId=" + reservationId
        );
    }
}
