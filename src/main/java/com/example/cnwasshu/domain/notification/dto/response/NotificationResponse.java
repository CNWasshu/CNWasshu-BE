package com.example.cnwasshu.domain.notification.dto.response;

import java.time.LocalDateTime;

import com.example.cnwasshu.domain.notification.entity.Notification;
import com.example.cnwasshu.domain.notification.entity.NotificationType;

public record NotificationResponse(
        Long notificationId,
        NotificationType notificationType,
        Long courseId,
        Long reservationId,
        Long activityId,
        Long courseSurveyId,
        String title,
        String content,
        boolean isRead,
        LocalDateTime createdAt
) {

    public static NotificationResponse from(Notification notification) {
        return new NotificationResponse(
                notification.getId(),
                notification.getNotificationType(),
                notification.getCourseId(),
                notification.getReservationId(),
                notification.getActivityId(),
                notification.getCourseSurveyId(),
                notification.getTitle(),
                notification.getContent(),
                notification.isRead(),
                notification.getCreatedAt()
        );
    }
}
