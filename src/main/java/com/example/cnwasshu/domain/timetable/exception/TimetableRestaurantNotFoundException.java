package com.example.cnwasshu.domain.timetable.exception;

import com.example.cnwasshu.common.exception.BusinessException;
import com.example.cnwasshu.common.exception.ErrorCode;

public class TimetableRestaurantNotFoundException extends BusinessException {

    public TimetableRestaurantNotFoundException(Long restaurantId) {
        super(
                ErrorCode.TIMETABLE_RESTAURANT_NOT_FOUND,
                "음식점을 찾을 수 없습니다. restaurantId=" + restaurantId
        );
    }
}
