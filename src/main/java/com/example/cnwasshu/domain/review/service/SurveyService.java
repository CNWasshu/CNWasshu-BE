package com.example.cnwasshu.domain.review.service;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Map;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.example.cnwasshu.common.exception.BusinessException;
import com.example.cnwasshu.common.exception.ErrorCode;
import com.example.cnwasshu.domain.course.entity.Course;
import com.example.cnwasshu.domain.course.entity.CourseItem;
import com.example.cnwasshu.domain.course.exception.CourseNotFoundException;
import com.example.cnwasshu.domain.course.repository.CourseRepository;
import com.example.cnwasshu.domain.review.dto.response.ActivitySurveyResponse;
import com.example.cnwasshu.domain.review.dto.response.SurveyDetailResponse;
import com.example.cnwasshu.domain.review.dto.request.ActivitySurveyDraftRequest;
import com.example.cnwasshu.domain.review.dto.request.SurveyDraftRequest;
import com.example.cnwasshu.domain.review.entity.ActivitySurvey;
import com.example.cnwasshu.domain.review.entity.CourseSurvey;
import com.example.cnwasshu.domain.review.entity.CourseUsageStatus;
import com.example.cnwasshu.domain.review.entity.SurveyStatus;
import com.example.cnwasshu.domain.review.entity.SurveyType;
import com.example.cnwasshu.domain.review.repository.ActivitySurveyRepository;
import com.example.cnwasshu.domain.review.repository.CourseSurveyRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SurveyService {

    private static final ZoneId KOREA_ZONE_ID = ZoneId.of("Asia/Seoul");
    private static final LocalTime REMINDER_TIME = LocalTime.of(10, 0);

    private final CourseSurveyRepository courseSurveyRepository;
    private final ActivitySurveyRepository activitySurveyRepository;
    private final CourseRepository courseRepository;
    private final ObjectMapper objectMapper;

    public SurveyDetailResponse getSurvey(Long userId, Long surveyId) {
        CourseSurvey survey = courseSurveyRepository.findByIdAndUserId(surveyId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SURVEY_NOT_FOUND));

        Course course = courseRepository.findByIdAndUserIdAndDeletedAtIsNull(survey.getCourseId(), userId)
                .orElseThrow(() -> new CourseNotFoundException(survey.getCourseId()));

        Map<Long, CourseItem> courseItemsById = course.getItems().stream()
                .collect(Collectors.toMap(CourseItem::getId, Function.identity()));

        var activities = activitySurveyRepository.findAllByCourseSurveyIdOrderByCourseItemId(surveyId).stream()
                .map(activitySurvey -> ActivitySurveyResponse.of(
                        activitySurvey,
                        resolveActivityTitle(courseItemsById, activitySurvey.getCourseItemId())
                ))
                .toList();

        return SurveyDetailResponse.of(survey, course.getCourseName(), activities);
    }

    @Transactional
    public SurveyDetailResponse saveDraft(Long userId, Long surveyId, SurveyDraftRequest request) {
        CourseSurvey survey = courseSurveyRepository.findByIdAndUserId(surveyId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SURVEY_NOT_FOUND));

        if (!survey.isEditable()) {
            throw new BusinessException(ErrorCode.SURVEY_NOT_EDITABLE);
        }

        survey.updateDraft(
                request.courseUsageStatus(),
                request.overallScore(),
                request.coursePace(),
                serializeTags(request.issueTags()),
                serializeTags(request.notUsedReasonTags()),
                request.comment()
        );

        if (request.activities() != null) {
            request.activities().forEach(activityRequest -> updateActivityDraft(surveyId, activityRequest));
        }

        return getSurvey(userId, surveyId);
    }

    @Transactional
    public SurveyDetailResponse submit(Long userId, Long surveyId) {
        CourseSurvey survey = courseSurveyRepository.findByIdAndUserId(surveyId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SURVEY_NOT_FOUND));

        if (!survey.isEditable()) {
            throw new BusinessException(ErrorCode.SURVEY_NOT_EDITABLE);
        }

        validateForSubmission(survey);
        survey.complete();

        return getSurvey(userId, surveyId);
    }

    @Transactional
    public SurveyDetailResponse snooze(Long userId, Long surveyId) {
        CourseSurvey survey = courseSurveyRepository.findByIdAndUserId(surveyId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SURVEY_NOT_FOUND));

        if (!survey.isEditable()) {
            throw new BusinessException(ErrorCode.SURVEY_NOT_EDITABLE);
        }
        if (survey.getStatus() == SurveyStatus.SNOOZED) {
            throw new BusinessException(ErrorCode.SURVEY_ALREADY_SNOOZED);
        }

        LocalDateTime nextScheduledAt = LocalDateTime.now(KOREA_ZONE_ID)
                .toLocalDate()
                .plusDays(1)
                .atTime(REMINDER_TIME);
        survey.snooze(nextScheduledAt);

        return getSurvey(userId, surveyId);
    }

    private void validateForSubmission(CourseSurvey survey) {
        if (survey.getSurveyType() == SurveyType.USER_COURSE) {
            if (survey.getCoursePace() == null) {
                throw new BusinessException(ErrorCode.INVALID_SURVEY_RESPONSE);
            }
            return;
        }

        CourseUsageStatus usageStatus = survey.getCourseUsageStatus();
        if (usageStatus == null) {
            throw new BusinessException(ErrorCode.INVALID_SURVEY_RESPONSE);
        }

        if (usageStatus != CourseUsageStatus.NOT_USED
                && (survey.getOverallScore() == null || survey.getCoursePace() == null)) {
            throw new BusinessException(ErrorCode.INVALID_SURVEY_RESPONSE);
        }
    }

    private void updateActivityDraft(Long surveyId, ActivitySurveyDraftRequest request) {
        ActivitySurvey activitySurvey = activitySurveyRepository
                .findByCourseSurveyIdAndCourseItemId(surveyId, request.courseItemId())
                .orElseThrow(() -> new BusinessException(ErrorCode.ACTIVITY_SURVEY_NOT_FOUND));

        activitySurvey.updateDraft(
                request.visitStatus(),
                request.recommended(),
                request.satisfactionScore(),
                serializeTags(request.reasonTags()),
                request.comment()
        );
    }

    private String serializeTags(List<String> tags) {
        if (tags == null) {
            return null;
        }

        try {
            return objectMapper.writeValueAsString(tags);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("만족도 조사 태그 형식이 올바르지 않습니다.", e);
        }
    }

    private String resolveActivityTitle(Map<Long, CourseItem> courseItemsById, Long courseItemId) {
        CourseItem courseItem = courseItemsById.get(courseItemId);
        return courseItem != null ? courseItem.getTitle() : null;
    }
}
