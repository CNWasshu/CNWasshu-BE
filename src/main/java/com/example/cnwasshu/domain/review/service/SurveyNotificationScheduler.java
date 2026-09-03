package com.example.cnwasshu.domain.review.service;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.example.cnwasshu.domain.course.repository.CourseRepository;
import com.example.cnwasshu.domain.notification.service.NotificationService;
import com.example.cnwasshu.domain.review.entity.CourseSurvey;
import com.example.cnwasshu.domain.review.entity.SurveyStatus;
import com.example.cnwasshu.domain.review.repository.CourseSurveyRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SurveyNotificationScheduler {

    private static final ZoneId KOREA_ZONE_ID = ZoneId.of("Asia/Seoul");
    private static final LocalTime FOLLOW_UP_TIME = LocalTime.of(10, 0);
    private static final List<SurveyStatus> DUE_STATUSES = List.of(
            SurveyStatus.SCHEDULED,
            SurveyStatus.SENT,
            SurveyStatus.OPENED,
            SurveyStatus.IN_PROGRESS,
            SurveyStatus.SNOOZED
    );

    private final CourseSurveyRepository courseSurveyRepository;
    private final CourseRepository courseRepository;
    private final NotificationService notificationService;

    @Scheduled(cron = "0 * * * * *", zone = "Asia/Seoul")
    @Transactional
    public void processDueSurveys() {
        LocalDateTime now = LocalDateTime.now(KOREA_ZONE_ID);
        courseSurveyRepository.findAllByStatusInAndScheduledAtLessThanEqual(DUE_STATUSES, now)
                .forEach(survey -> process(survey, now));
    }

    void process(CourseSurvey survey, LocalDateTime now) {
        boolean activeCourse = courseRepository
                .findByIdAndUserIdAndDeletedAtIsNull(survey.getCourseId(), survey.getUserId())
                .isPresent();
        if (!activeCourse || !notificationService.isSurveyNotificationEnabled(survey.getUserId())) {
            survey.cancelIfIncomplete();
            return;
        }

        if (survey.getStatus() == SurveyStatus.SCHEDULED) {
            notificationService.createSurveyNotification(
                    survey.getUserId(),
                    survey.getId(),
                    survey.getSurveyType()
            );
            survey.markSent(now, nextMorning(now));
            return;
        }

        if (survey.getReminderCount() == 0) {
            notificationService.createSurveyReminderNotification(
                    survey.getUserId(),
                    survey.getId(),
                    survey.getSurveyType()
            );
            survey.markReminderSent(nextMorning(now));
            return;
        }

        survey.expire();
    }

    private LocalDateTime nextMorning(LocalDateTime now) {
        return now.toLocalDate().plusDays(1).atTime(FOLLOW_UP_TIME);
    }
}
