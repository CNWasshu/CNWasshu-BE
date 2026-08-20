package com.example.cnwasshu.domain.timetable.exception;

import com.example.cnwasshu.common.exception.BusinessException;
import com.example.cnwasshu.common.exception.ErrorCode;

public class ActivityOutsideOperatingHoursException extends BusinessException {

    public ActivityOutsideOperatingHoursException(Long activityId) {
        super(
                ErrorCode.ACTIVITY_OUTSIDE_OPERATING_HOURS,
                "체험 운영 시간 안에서 일정을 선택해 주세요. activityId=" + activityId
        );
    }
}
