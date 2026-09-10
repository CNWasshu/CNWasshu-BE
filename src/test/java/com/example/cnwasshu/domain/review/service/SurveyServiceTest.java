package com.example.cnwasshu.domain.review.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.cnwasshu.common.exception.BusinessException;
import com.example.cnwasshu.common.exception.ErrorCode;
import com.example.cnwasshu.domain.course.entity.Course;
import com.example.cnwasshu.domain.course.entity.CourseType;
import com.example.cnwasshu.domain.course.repository.CourseRepository;
import com.example.cnwasshu.domain.review.dto.request.SurveyDraftRequest;
import com.example.cnwasshu.domain.review.dto.response.SurveyDetailResponse;
import com.example.cnwasshu.domain.review.entity.CoursePace;
import com.example.cnwasshu.domain.review.entity.CourseSurvey;
import com.example.cnwasshu.domain.review.entity.CourseUsageStatus;
import com.example.cnwasshu.domain.review.entity.SurveyStatus;
import com.example.cnwasshu.domain.review.entity.SurveyType;
import com.example.cnwasshu.domain.review.repository.ActivitySurveyRepository;
import com.example.cnwasshu.domain.review.repository.CourseSurveyRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
class SurveyServiceTest {

    private static final Long USER_ID = 1L;
    private static final Long SURVEY_ID = 10L;
    private static final Long COURSE_ID = 20L;
    private static final ZoneId KOREA_ZONE_ID = ZoneId.of("Asia/Seoul");

    @Mock
    private CourseSurveyRepository courseSurveyRepository;

    @Mock
    private ActivitySurveyRepository activitySurveyRepository;

    @Mock
    private CourseRepository courseRepository;

    private SurveyService surveyService;

    @BeforeEach
    void setUp() {
        surveyService = new SurveyService(
                courseSurveyRepository,
                activitySurveyRepository,
                courseRepository,
                new ObjectMapper()
        );
    }

    @Test
    void 본인의_만족도_조사를_조회한다() {
        CourseSurvey survey = userCourseSurvey();
        stubSurveyDetail(survey);

        SurveyDetailResponse response = surveyService.getSurvey(USER_ID, SURVEY_ID);

        assertThat(response.courseId()).isEqualTo(COURSE_ID);
        assertThat(response.courseName()).isEqualTo("공주 여행");
        assertThat(response.surveyType()).isEqualTo(SurveyType.USER_COURSE);
        assertThat(response.activities()).isEmpty();
    }

    @Test
    void 존재하지_않거나_다른_사용자의_조사는_조회할_수_없다() {
        when(courseSurveyRepository.findByIdAndUserId(SURVEY_ID, USER_ID))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> surveyService.getSurvey(USER_ID, SURVEY_ID))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.SURVEY_NOT_FOUND);
    }

    @Test
    void 임시_저장하면_응답과_상태가_갱신된다() {
        CourseSurvey survey = userCourseSurvey();
        stubSurveyDetail(survey);
        SurveyDraftRequest request = new SurveyDraftRequest(
                null,
                null,
                CoursePace.APPROPRIATE,
                List.of("이동 거리가 적절했어요"),
                null,
                "좋은 코스였어요",
                null
        );

        SurveyDetailResponse response = surveyService.saveDraft(USER_ID, SURVEY_ID, request);

        assertThat(response.status()).isEqualTo(SurveyStatus.IN_PROGRESS);
        assertThat(response.coursePace()).isEqualTo(CoursePace.APPROPRIATE);
        assertThat(response.issueTags()).isEqualTo("[\"이동 거리가 적절했어요\"]");
        assertThat(response.comment()).isEqualTo("좋은 코스였어요");
    }

    @Test
    void 직접_코스는_일정_평가를_저장한_후_제출할_수_있다() {
        CourseSurvey survey = userCourseSurvey();
        survey.updateDraft(null, null, CoursePace.APPROPRIATE, null, null, null);
        stubSurveyDetail(survey);

        SurveyDetailResponse response = surveyService.submit(USER_ID, SURVEY_ID);

        assertThat(response.status()).isEqualTo(SurveyStatus.COMPLETED);
        assertThat(response.completedAt()).isNotNull();
    }

    @Test
    void 직접_코스의_일정_평가가_없으면_제출할_수_없다() {
        CourseSurvey survey = userCourseSurvey();
        stubOwnedSurvey(survey);

        assertThatThrownBy(() -> surveyService.submit(USER_ID, SURVEY_ID))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_SURVEY_RESPONSE);
    }

    @Test
    void 이용하지_않은_AI_코스는_NOT_USED로_제출된다() {
        CourseSurvey survey = aiCourseSurvey();
        survey.updateDraft(CourseUsageStatus.NOT_USED, null, null, null, null, null);
        stubSurveyDetail(survey);

        SurveyDetailResponse response = surveyService.submit(USER_ID, SURVEY_ID);

        assertThat(response.status()).isEqualTo(SurveyStatus.NOT_USED);
        assertThat(response.completedAt()).isNotNull();
    }

    @Test
    void AI_코스를_이용했다면_전반_만족도와_일정_평가가_필요하다() {
        CourseSurvey survey = aiCourseSurvey();
        survey.updateDraft(CourseUsageStatus.MOSTLY_USED, null, null, null, null, null);
        stubOwnedSurvey(survey);

        assertThatThrownBy(() -> surveyService.submit(USER_ID, SURVEY_ID))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_SURVEY_RESPONSE);
    }

    @Test
    void 재알림은_한국_시간_기준_다음날_오전_10시로_예약된다() {
        CourseSurvey survey = userCourseSurvey();
        stubSurveyDetail(survey);
        LocalDate today = LocalDate.now(KOREA_ZONE_ID);

        SurveyDetailResponse response = surveyService.snooze(USER_ID, SURVEY_ID);

        assertThat(response.status()).isEqualTo(SurveyStatus.SNOOZED);
        assertThat(response.scheduledAt().toLocalDate()).isEqualTo(today.plusDays(1));
        assertThat(response.scheduledAt().toLocalTime()).isEqualTo(LocalTime.of(10, 0));
    }

    @Test
    void 이미_미룬_조사는_다시_미룰_수_없다() {
        CourseSurvey survey = userCourseSurvey();
        survey.snooze(LocalDateTime.now().plusDays(1));
        stubOwnedSurvey(survey);

        assertThatThrownBy(() -> surveyService.snooze(USER_ID, SURVEY_ID))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.SURVEY_ALREADY_SNOOZED);
    }

    private CourseSurvey userCourseSurvey() {
        return CourseSurvey.schedule(
                USER_ID,
                COURSE_ID,
                LocalDate.of(2026, 9, 3),
                SurveyType.USER_COURSE,
                LocalDateTime.of(2026, 9, 3, 19, 0)
        );
    }

    private CourseSurvey aiCourseSurvey() {
        return CourseSurvey.schedule(
                USER_ID,
                COURSE_ID,
                LocalDate.of(2026, 9, 3),
                SurveyType.AI_COURSE,
                LocalDateTime.of(2026, 9, 3, 19, 0)
        );
    }

    private void stubSurveyDetail(CourseSurvey survey) {
        stubOwnedSurvey(survey);
        when(courseRepository.findByIdAndUserIdAndDeletedAtIsNull(COURSE_ID, USER_ID))
                .thenReturn(Optional.of(course()));
        when(activitySurveyRepository.findAllByCourseSurveyIdOrderByCourseItemId(SURVEY_ID))
                .thenReturn(List.of());
    }

    private void stubOwnedSurvey(CourseSurvey survey) {
        when(courseSurveyRepository.findByIdAndUserId(SURVEY_ID, USER_ID))
                .thenReturn(Optional.of(survey));
    }

    private Course course() {
        return Course.builder()
                .userId(USER_ID)
                .courseName("공주 여행")
                .courseType(CourseType.USER)
                .peopleCount(2)
                .withChild(false)
                .startDate(LocalDate.of(2026, 9, 3))
                .endDate(LocalDate.of(2026, 9, 3))
                .build();
    }
}
