package com.example.cnwasshu.domain.timetable.exception;

import com.example.cnwasshu.common.exception.BusinessException;
import com.example.cnwasshu.common.exception.ErrorCode;

public class TimetableScheduleRequiredException extends BusinessException {

    public TimetableScheduleRequiredException() {
        super(ErrorCode.TIMETABLE_SCHEDULE_REQUIRED);
    }
}
