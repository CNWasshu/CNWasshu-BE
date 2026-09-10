package com.example.cnwasshu.domain.notification.entity;

import com.example.cnwasshu.common.entity.BaseTimeEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * notificationType에 따라 아래 참조 ID 중 정확히 하나만 채워진다:
 * - COURSE      -> courseId 만 채움 (reservationId, activityId는 null)
 * - RESERVATION -> reservationId 만 채움 (courseId, activityId는 null)
 * - SURVEY      -> courseSurveyId 만 채움
 * User/Course/Reservation/Activity를 @ManyToOne으로 직접 연관관계 맺지 않고
 * 순수 ID 컬럼으로 두는 이유는 도메인 간 결합을 줄이기 위함(Course.userId와 동일한 컨벤션).
 */
@Getter
@Entity
@Table(name = "notification")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Notification extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notification_id")
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "reservation_id")
    private Long reservationId;

    @Column(name = "course_id")
    private Long courseId;

    @Column(name = "activity_id")
    private Long activityId;

    @Column(name = "course_survey_id")
    private Long courseSurveyId;

    @Enumerated(EnumType.STRING)
    @Column(name = "notification_type", nullable = false, length = 20)
    private NotificationType notificationType;

    @Column(name = "title", nullable = false, length = 100)
    private String title;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "is_read", nullable = false)
    private boolean isRead = false;

    private Notification(Long userId, NotificationType notificationType, Long reservationId,
                          Long courseId, Long activityId, Long courseSurveyId,
                          String title, String content) {
        this.userId = userId;
        this.notificationType = notificationType;
        this.reservationId = reservationId;
        this.courseId = courseId;
        this.activityId = activityId;
        this.courseSurveyId = courseSurveyId;
        this.title = title;
        this.content = content;
    }

    public static Notification forCourse(Long userId, Long courseId, String title, String content) {
        return new Notification(userId, NotificationType.COURSE, null, courseId, null, null, title, content);
    }

    public static Notification forReservation(Long userId, Long reservationId, String title, String content) {
        return new Notification(userId, NotificationType.RESERVATION, reservationId, null, null, null, title, content);
    }

    public static Notification forSurvey(Long userId, Long courseSurveyId, String title, String content) {
        return new Notification(userId, NotificationType.SURVEY, null, null, null, courseSurveyId, title, content);
    }

    public void markAsRead() {
        this.isRead = true;
    }
}
