package com.example.cnwasshu.domain.user.dto.response;

import com.example.cnwasshu.domain.user.entity.UserDevice;

public record UserDeviceResponse(
        Long deviceId,
        String deviceType
) {

    public static UserDeviceResponse from(UserDevice device) {
        String deviceType = device.getDeviceType() != null ? device.getDeviceType().name() : null;
        return new UserDeviceResponse(device.getId(), deviceType);
    }
}
