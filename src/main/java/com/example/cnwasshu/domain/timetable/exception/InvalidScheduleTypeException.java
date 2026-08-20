package com.example.cnwasshu.domain.timetable.exception;

import com.example.cnwasshu.common.exception.BusinessException;
import com.example.cnwasshu.common.exception.ErrorCode;

public class InvalidScheduleTypeException extends BusinessException {

    public InvalidScheduleTypeException(String clientScheduleId) {
        super(
                ErrorCode.INVALID_SCHEDULE_TYPE,
                "일정 종류와 activityId, reservationId 조합이 올바르지 않습니다. clientScheduleId="
                        + clientScheduleId
        );
    }
}
