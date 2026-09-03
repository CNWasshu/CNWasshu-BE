package com.example.cnwasshu.domain.user.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.cnwasshu.common.exception.BusinessException;
import com.example.cnwasshu.common.exception.ErrorCode;
import com.example.cnwasshu.domain.user.dto.request.DeviceRegisterRequest;
import com.example.cnwasshu.domain.user.dto.request.UserUpdateRequest;
import com.example.cnwasshu.domain.user.dto.response.UserDeviceResponse;
import com.example.cnwasshu.domain.user.dto.response.UserSummary;
import com.example.cnwasshu.domain.user.entity.User;
import com.example.cnwasshu.domain.user.entity.UserDevice;
import com.example.cnwasshu.domain.user.repository.RefreshTokenRepository;
import com.example.cnwasshu.domain.user.repository.UserDeviceRepository;
import com.example.cnwasshu.domain.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final UserDeviceRepository userDeviceRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    public UserSummary getMe(Long userId) {
        return UserSummary.from(getActiveUser(userId));
    }

    public UserSummary updateMe(Long userId, UserUpdateRequest request) {
        User user = getActiveUser(userId);

        String nickname = user.getNickname();
        if (request.nickname() != null) {
            if (request.nickname().isBlank()) {
                throw new BusinessException(ErrorCode.INVALID_NICKNAME);
            }
            nickname = request.nickname();
        }

        user.updateProfile(nickname, user.getEmail());
        return UserSummary.from(user);
    }

    public void deleteMe(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        user.softDelete();
        refreshTokenRepository.deleteAllByUserId(userId);
    }

    public UserDeviceResponse registerDevice(Long userId, DeviceRegisterRequest request) {
        User user = getActiveUser(userId);

        UserDevice device = userDeviceRepository.findByFcmToken(request.fcmToken())
                .map(existing -> {
                    existing.updateOwner(user, request.deviceType());
                    return existing;
                })
                .orElseGet(() -> userDeviceRepository.save(
                        UserDevice.of(user, request.fcmToken(), request.deviceType())
                ));

        return UserDeviceResponse.from(device);
    }

    private User getActiveUser(Long userId) {
        return userRepository.findById(userId)
                .filter(user -> !user.isDeleted())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }
}
