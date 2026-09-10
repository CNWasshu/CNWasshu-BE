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
import com.example.cnwasshu.domain.notification.entity.ReservationReminderType;
import com.example.cnwasshu.domain.notification.repository.NotificationRepository;
import com.example.cnwasshu.domain.notification.repository.NotificationSettingRepository;
import com.example.cnwasshu.domain.review.entity.SurveyType;

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
                resolve(request.reservation30mBefore(), setting.isReservation30mBefore()),
                resolve(request.surveyEnabled(), setting.isSurveyEnabled())
        );

        return NotificationSettingResponse.from(setting);
    }

    public boolean createSurveyNotification(
            Long userId,
            Long courseSurveyId,
            SurveyType surveyType
    ) {
        NotificationSetting setting = getOrCreateSetting(userId);
        if (!setting.isSurveyEnabled() || notificationRepository.existsByCourseSurveyId(courseSurveyId)) {
            return false;
        }

        String title = surveyType == SurveyType.AI_COURSE
                ? "AI가 추천한 오늘의 코스는 어떠셨나요?"
                : "오늘 충남 여행은 어떠셨나요?";
        String content = surveyType == SurveyType.AI_COURSE
                ? "일정과 이동이 적절했는지 알려주세요."
                : "방문한 체험을 1분 안에 평가해 주세요.";

        notificationRepository.save(Notification.forSurvey(
                userId,
                courseSurveyId,
                title,
                content
        ));
        return true;
    }

    public boolean isSurveyNotificationEnabled(Long userId) {
        return getOrCreateSetting(userId).isSurveyEnabled();
    }

    public void createSurveyReminderNotification(
            Long userId,
            Long courseSurveyId,
            SurveyType surveyType
    ) {
        String title = surveyType == SurveyType.AI_COURSE
                ? "AI 추천 코스 만족도 조사를 잊지 않으셨나요?"
                : "여행 만족도 조사를 잊지 않으셨나요?";

        notificationRepository.save(Notification.forSurvey(
                userId,
                courseSurveyId,
                title,
                "잠시 시간을 내어 여행 경험을 알려주세요."
        ));
    }

    public boolean createReservationReminderNotification(
            Long userId,
            Long reservationId,
            String activityTitle,
            ReservationReminderType reminderType
    ) {
        NotificationSetting setting =
                getOrCreateSetting(userId);

        if (!isReservationReminderEnabled(
                setting,
                reminderType
        )) {
            return false;
        }

        if (notificationRepository
                .existsByReservationIdAndTitle(
                        reservationId,
                        reminderType.getTitle()
                )) {
            return false;
        }

        notificationRepository.save(
                Notification.forReservation(
                        userId,
                        reservationId,
                        reminderType.getTitle(),
                        reminderType.createContent(
                                activityTitle
                        )
                )
        );

        return true;
    }

    private boolean isReservationReminderEnabled(
            NotificationSetting setting,
            ReservationReminderType reminderType
    ) {
        return switch (reminderType) {
            case DAY_BEFORE ->
                    setting.isReservationDayBefore();

            case THREE_HOURS_BEFORE ->
                    setting.isReservation3hBefore();

            case ONE_HOUR_BEFORE ->
                    setting.isReservation1hBefore();

            case THIRTY_MINUTES_BEFORE ->
                    setting.isReservation30mBefore();
        };
    }

    private NotificationSetting getOrCreateSetting(Long userId) {
        return notificationSettingRepository.findByUserId(userId)
                .orElseGet(() -> notificationSettingRepository.save(NotificationSetting.createDefault(userId)));
    }

    private boolean resolve(Boolean requested, boolean current) {
        return requested != null ? requested : current;
    }
}
