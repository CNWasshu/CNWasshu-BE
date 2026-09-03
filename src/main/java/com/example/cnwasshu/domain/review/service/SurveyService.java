package com.example.cnwasshu.domain.review.service;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.cnwasshu.common.exception.BusinessException;
import com.example.cnwasshu.common.exception.ErrorCode;
import com.example.cnwasshu.domain.course.entity.Course;
import com.example.cnwasshu.domain.course.entity.CourseItem;
import com.example.cnwasshu.domain.course.exception.CourseNotFoundException;
import com.example.cnwasshu.domain.course.repository.CourseRepository;
import com.example.cnwasshu.domain.review.dto.response.ActivitySurveyResponse;
import com.example.cnwasshu.domain.review.dto.response.SurveyDetailResponse;
import com.example.cnwasshu.domain.review.entity.CourseSurvey;
import com.example.cnwasshu.domain.review.repository.ActivitySurveyRepository;
import com.example.cnwasshu.domain.review.repository.CourseSurveyRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SurveyService {

    private final CourseSurveyRepository courseSurveyRepository;
    private final ActivitySurveyRepository activitySurveyRepository;
    private final CourseRepository courseRepository;

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

    private String resolveActivityTitle(Map<Long, CourseItem> courseItemsById, Long courseItemId) {
        CourseItem courseItem = courseItemsById.get(courseItemId);
        return courseItem != null ? courseItem.getTitle() : null;
    }
}
