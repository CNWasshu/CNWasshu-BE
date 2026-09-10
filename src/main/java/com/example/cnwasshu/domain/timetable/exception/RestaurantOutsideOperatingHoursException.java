package com.example.cnwasshu.domain.timetable.exception;

import com.example.cnwasshu.common.exception.BusinessException;
import com.example.cnwasshu.common.exception.ErrorCode;

public class RestaurantOutsideOperatingHoursException extends BusinessException {

    public RestaurantOutsideOperatingHoursException(Long restaurantId) {
        super(
                ErrorCode.RESTAURANT_OUTSIDE_OPERATING_HOURS,
                "음식점 운영 시간 안에서 일정을 선택해 주세요. restaurantId=" + restaurantId
        );
    }
}
