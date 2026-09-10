package com.example.cnwasshu.domain.review.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.example.cnwasshu.domain.review.entity.CoursePace;
import com.example.cnwasshu.domain.review.entity.CourseSurvey;
import com.example.cnwasshu.domain.review.entity.CourseUsageStatus;
import com.example.cnwasshu.domain.review.entity.SurveyStatus;
import com.example.cnwasshu.domain.review.entity.SurveyType;

public record SurveyDetailResponse(
        Long surveyId,
        Long courseId,
        String courseName,
        LocalDate courseDate,
        SurveyType surveyType,
        SurveyStatus status,
        LocalDateTime scheduledAt,
        LocalDateTime sentAt,
        LocalDateTime openedAt,
        LocalDateTime completedAt,
        int reminderCount,
        CourseUsageStatus courseUsageStatus,
        Integer overallScore,
        CoursePace coursePace,
        String issueTags,
        String notUsedReasonTags,
        String comment,
        List<ActivitySurveyResponse> activities
) {

    public static SurveyDetailResponse of(
            CourseSurvey survey,
            String courseName,
            List<ActivitySurveyResponse> activities
    ) {
        return new SurveyDetailResponse(
                survey.getId(),
                survey.getCourseId(),
                courseName,
                survey.getCourseDate(),
                survey.getSurveyType(),
                survey.getStatus(),
                survey.getScheduledAt(),
                survey.getSentAt(),
                survey.getOpenedAt(),
                survey.getCompletedAt(),
                survey.getReminderCount(),
                survey.getCourseUsageStatus(),
                survey.getOverallScore(),
                survey.getCoursePace(),
                survey.getIssueTags(),
                survey.getNotUsedReasonTags(),
                survey.getComment(),
                activities
        );
    }
}
