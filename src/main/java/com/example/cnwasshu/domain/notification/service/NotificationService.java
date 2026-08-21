package com.example.cnwasshu.domain.notification.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.cnwasshu.common.exception.BusinessException;
import com.example.cnwasshu.common.exception.ErrorCode;
import com.example.cnwasshu.domain.notification.dto.request.NotificationSettingUpdateRequest;
import com.example.cnwasshu.domain.notification.dto.response.NotificationListResponse;
import com.example.cnwasshu.domain.notification.dto.response.NotificationSettingResponse;
import com.example.cnwasshu.domain.notification.entity.Notification;
import com.example.cnwasshu.domain.notification.entity.NotificationSetting;
import com.example.cnwasshu.domain.notification.repository.NotificationRepository;
import com.example.cnwasshu.domain.notification.repository.NotificationSettingRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationSettingRepository notificationSettingRepository;

    @Transactional(readOnly = true)
    public NotificationListResponse getMyNotifications(Long userId, Pageable pageable) {
        Page<Notification> notifications = notificationRepository.findByUserId(userId, pageable);
        return NotificationListResponse.from(notifications);
    }

    public void markAsRead(Long userId, Long notificationId) {
        Notification notification = notificationRepository.findByIdAndUserId(notificationId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOTIFICATION_NOT_FOUND));
        notification.markAsRead();
    }

    public NotificationSettingResponse getMySettings(Long userId) {
        return NotificationSettingResponse.from(getOrCreateSetting(userId));
    }

    public NotificationSettingResponse updateMySettings(Long userId, NotificationSettingUpdateRequest request) {
        NotificationSetting setting = getOrCreateSetting(userId);

        setting.update(
                resolve(request.courseDayBefore(), setting.isCourseDayBefore()),
                resolve(request.reservationDayBefore(), setting.isReservationDayBefore()),
                resolve(request.reservation3hBefore(), setting.isReservation3hBefore()),
                resolve(request.reservation1hBefore(), setting.isReservation1hBefore()),
                resolve(request.reservation30mBefore(), setting.isReservation30mBefore())
        );

        return NotificationSettingResponse.from(setting);
    }

    private NotificationSetting getOrCreateSetting(Long userId) {
        return notificationSettingRepository.findByUserId(userId)
                .orElseGet(() -> notificationSettingRepository.save(NotificationSetting.createDefault(userId)));
    }

    private boolean resolve(Boolean requested, boolean current) {
        return requested != null ? requested : current;
    }
}
