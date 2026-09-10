package com.example.cnwasshu.domain.user.dto.request;

import jakarta.validation.constraints.NotNull;

public record OnboardingCompleteRequest(
        @NotNull(message = "completionType은 필수입니다.") CompletionType completionType
) {

    public enum CompletionType {
        COMPLETED,
        SKIPPED
    }
}
