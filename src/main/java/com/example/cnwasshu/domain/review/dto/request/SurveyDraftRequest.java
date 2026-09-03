package com.example.cnwasshu.domain.review.dto.request;

import java.util.List;

import com.example.cnwasshu.domain.review.entity.CoursePace;
import com.example.cnwasshu.domain.review.entity.CourseUsageStatus;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

public record SurveyDraftRequest(
        CourseUsageStatus courseUsageStatus,
        @Min(1) @Max(5) Integer overallScore,
        CoursePace coursePace,
        List<String> issueTags,
        List<String> notUsedReasonTags,
        @Size(max = 300) String comment,
        List<@Valid ActivitySurveyDraftRequest> activities
) {
}
