package com.example.cnwasshu.domain.review.dto.response;

import com.example.cnwasshu.domain.review.entity.ActivitySurvey;
import com.example.cnwasshu.domain.review.entity.VisitEvidenceType;
import com.example.cnwasshu.domain.review.entity.VisitStatus;

public record ActivitySurveyResponse(
        Long surveyId,
        Long courseItemId,
        Long activityId,
        String activityTitle,
        VisitStatus visitStatus,
        VisitEvidenceType visitEvidenceType,
        Boolean recommended,
        Integer satisfactionScore,
        String reasonTags,
        String comment
) {

    public static ActivitySurveyResponse of(ActivitySurvey survey, String activityTitle) {
        return new ActivitySurveyResponse(
                survey.getId(),
                survey.getCourseItemId(),
                survey.getActivityId(),
                activityTitle,
                survey.getVisitStatus(),
                survey.getVisitEvidenceType(),
                survey.getRecommended(),
                survey.getSatisfactionScore(),
                survey.getReasonTags(),
                survey.getComment()
        );
    }
}
