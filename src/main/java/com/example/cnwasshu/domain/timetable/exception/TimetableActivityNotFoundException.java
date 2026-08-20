package com.example.cnwasshu.domain.timetable.exception;

import com.example.cnwasshu.common.exception.BusinessException;
import com.example.cnwasshu.common.exception.ErrorCode;

public class TimetableActivityNotFoundException extends BusinessException {

    public TimetableActivityNotFoundException(Long activityId) {
        super(
                ErrorCode.TIMETABLE_ACTIVITY_NOT_FOUND,
                "체험을 찾을 수 없습니다. activityId=" + activityId
        );
    }
}
