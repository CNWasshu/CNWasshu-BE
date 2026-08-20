package com.example.cnwasshu.domain.timetable.exception;

import com.example.cnwasshu.common.exception.BusinessException;
import com.example.cnwasshu.common.exception.ErrorCode;

public class TimetableNotFoundException extends BusinessException {

    public TimetableNotFoundException(Long timetableId) {
        super(ErrorCode.TIMETABLE_NOT_FOUND, "타임테이블을 찾을 수 없습니다. timetableId=" + timetableId);
    }
}
