package com.example.cnwasshu.domain.timetable.exception;

import com.example.cnwasshu.common.exception.BusinessException;
import com.example.cnwasshu.common.exception.ErrorCode;

public class ScheduleTimeConflictException extends BusinessException {

    public ScheduleTimeConflictException(
            Integer dayNo,
            String firstClientScheduleId,
            String secondClientScheduleId
    ) {
        super(
                ErrorCode.SCHEDULE_TIME_CONFLICT,
                dayNo + "일차에 시간이 겹치는 일정이 있습니다. clientScheduleIds="
                        + firstClientScheduleId + "," + secondClientScheduleId
        );
    }
}
