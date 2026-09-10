package com.example.cnwasshu.domain.timetable.exception;

import com.example.cnwasshu.common.exception.BusinessException;
import com.example.cnwasshu.common.exception.ErrorCode;

public class InvalidScheduleTimeException extends BusinessException {

    public InvalidScheduleTimeException(String clientScheduleId) {
        super(
                ErrorCode.INVALID_SCHEDULE_TIME,
                "종료 시간은 시작 시간보다 늦어야 합니다. clientScheduleId=" + clientScheduleId
        );
    }
}
