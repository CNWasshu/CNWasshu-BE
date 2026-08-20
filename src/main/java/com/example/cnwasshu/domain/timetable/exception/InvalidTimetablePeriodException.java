package com.example.cnwasshu.domain.timetable.exception;

import com.example.cnwasshu.common.exception.BusinessException;
import com.example.cnwasshu.common.exception.ErrorCode;

public class InvalidTimetablePeriodException extends BusinessException {

    public InvalidTimetablePeriodException(String message) {
        super(ErrorCode.INVALID_TIMETABLE_PERIOD, message);
    }
}
