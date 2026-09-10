package com.example.cnwasshu.domain.review.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.example.cnwasshu.domain.course.entity.Course;
import com.example.cnwasshu.domain.course.repository.CourseRepository;
import com.example.cnwasshu.domain.notification.entity.Notification;
import com.example.cnwasshu.domain.notification.entity.NotificationSetting;
import com.example.cnwasshu.domain.notification.repository.NotificationRepository;
import com.example.cnwasshu.domain.notification.repository.NotificationSettingRepository;
import com.example.cnwasshu.domain.notification.service.NotificationService;
import com.example.cnwasshu.domain.review.entity.CourseSurvey;
import com.example.cnwasshu.domain.review.entity.SurveyStatus;
import com.example.cnwasshu.domain.review.entity.SurveyType;
import com.example.cnwasshu.domain.review.repository.CourseSurveyRepository;

@ExtendWith(MockitoExtension.class)
class SurveyNotificationSchedulerTest {

    private static final Long USER_ID = 1L;
    private static final Long COURSE_ID = 10L;
    private static final Long SURVEY_ID = 100L;
    private static final LocalDateTime NOW = LocalDateTime.of(2026, 9, 3, 19, 0);

    @Mock
    private CourseSurveyRepository courseSurveyRepository;

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private NotificationSettingRepository notificationSettingRepository;

    private SurveyNotificationScheduler scheduler;

    @BeforeEach
    void setUp() {
        NotificationService notificationService = new NotificationService(
                notificationRepository,
                notificationSettingRepository
        );
        scheduler = new SurveyNotificationScheduler(
                courseSurveyRepository,
                courseRepository,
                notificationService
        );
    }

    @Test
    void 발송_시각이_되면_내부_알림을_생성하고_SENT로_변경한다() {
        CourseSurvey survey = survey();
        stubActiveCourse();
        stubEnabledSetting();
        when(notificationRepository.existsByCourseSurveyId(SURVEY_ID)).thenReturn(false);

        scheduler.process(survey, NOW);

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(captor.capture());
        assertThat(captor.getValue().getCourseSurveyId()).isEqualTo(SURVEY_ID);
        assertThat(captor.getValue().getTitle()).isEqualTo("오늘 충남 여행은 어떠셨나요?");
        assertThat(survey.getStatus()).isEqualTo(SurveyStatus.SENT);
        assertThat(survey.getSentAt()).isEqualTo(NOW);
        assertThat(survey.getScheduledAt()).isEqualTo(
                LocalDateTime.of(2026, 9, 4, 10, 0)
        );
    }

    @Test
    void 최초_알림에_응답하지_않으면_재알림을_한번_생성한다() {
        CourseSurvey survey = survey();
        survey.markSent(NOW.minusDays(1), NOW);
        stubActiveCourse();
        stubEnabledSetting();

        scheduler.process(survey, NOW);

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(captor.capture());
        assertThat(captor.getValue().getTitle()).isEqualTo("여행 만족도 조사를 잊지 않으셨나요?");
        assertThat(survey.getReminderCount()).isEqualTo(1);
        assertThat(survey.getStatus()).isEqualTo(SurveyStatus.SENT);
        assertThat(survey.getScheduledAt()).isEqualTo(
                LocalDateTime.of(2026, 9, 4, 10, 0)
        );
    }

    @Test
    void 재알림_후에도_응답하지_않으면_EXPIRED로_변경한다() {
        CourseSurvey survey = survey();
        survey.markSent(NOW.minusDays(2), NOW.minusDays(1));
        survey.markReminderSent(NOW);
        stubActiveCourse();
        stubEnabledSetting();

        scheduler.process(survey, NOW);

        assertThat(survey.getStatus()).isEqualTo(SurveyStatus.EXPIRED);
        verify(notificationRepository, never()).save(any());
    }

    @Test
    void 삭제된_코스의_설문은_발송하지_않고_취소한다() {
        CourseSurvey survey = survey();
        when(courseRepository.findByIdAndUserIdAndDeletedAtIsNull(COURSE_ID, USER_ID))
                .thenReturn(Optional.empty());

        scheduler.process(survey, NOW);

        assertThat(survey.getStatus()).isEqualTo(SurveyStatus.CANCELED);
        verify(notificationRepository, never()).save(any());
    }

    @Test
    void 설문_알림을_거부한_사용자에게는_발송하지_않는다() {
        CourseSurvey survey = survey();
        NotificationSetting setting = NotificationSetting.createDefault(USER_ID);
        setting.update(true, true, true, true, true, false);
        stubActiveCourse();
        when(notificationSettingRepository.findByUserId(USER_ID)).thenReturn(Optional.of(setting));

        scheduler.process(survey, NOW);

        assertThat(survey.getStatus()).isEqualTo(SurveyStatus.CANCELED);
        verify(notificationRepository, never()).save(any());
    }

    private CourseSurvey survey() {
        CourseSurvey survey = CourseSurvey.schedule(
                USER_ID,
                COURSE_ID,
                LocalDate.of(2026, 9, 3),
                SurveyType.USER_COURSE,
                NOW
        );
        ReflectionTestUtils.setField(survey, "id", SURVEY_ID);
        return survey;
    }

    private void stubActiveCourse() {
        when(courseRepository.findByIdAndUserIdAndDeletedAtIsNull(COURSE_ID, USER_ID))
                .thenReturn(Optional.of(mock(Course.class)));
    }

    private void stubEnabledSetting() {
        when(notificationSettingRepository.findByUserId(USER_ID))
                .thenReturn(Optional.of(NotificationSetting.createDefault(USER_ID)));
    }
}
