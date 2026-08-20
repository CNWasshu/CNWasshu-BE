package com.example.cnwasshu.domain.timetable.exception;

import com.example.cnwasshu.common.exception.BusinessException;
import com.example.cnwasshu.common.exception.ErrorCode;

public class InvalidTimetableDayException extends BusinessException {

    public InvalidTimetableDayException(String message) {
        super(ErrorCode.INVALID_TIMETABLE_DAY, message);
    }
}
