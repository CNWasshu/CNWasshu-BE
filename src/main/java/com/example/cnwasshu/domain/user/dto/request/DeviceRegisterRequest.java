package com.example.cnwasshu.domain.user.dto.request;

import com.example.cnwasshu.domain.user.entity.UserDevice;

import jakarta.validation.constraints.NotBlank;

public record DeviceRegisterRequest(
        @NotBlank(message = "fcmToken은 필수입니다.") String fcmToken,
        UserDevice.DeviceType deviceType
) {
}
