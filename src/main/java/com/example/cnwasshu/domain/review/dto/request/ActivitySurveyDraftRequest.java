package com.example.cnwasshu.domain.review.dto.request;

import java.util.List;

import com.example.cnwasshu.domain.review.entity.VisitStatus;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ActivitySurveyDraftRequest(
        @NotNull Long courseItemId,
        VisitStatus visitStatus,
        Boolean recommended,
        @Min(1) @Max(5) Integer satisfactionScore,
        List<String> reasonTags,
        @Size(max = 300) String comment
) {
}
