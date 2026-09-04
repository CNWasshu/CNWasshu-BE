package com.example.cnwasshu.domain.user.dto.response;

import com.example.cnwasshu.domain.user.entity.OnboardingStatus;
import com.example.cnwasshu.domain.user.entity.User;
import java.time.LocalDateTime;

public record OnboardingStatusResponse(
        OnboardingStatus onboardingStatus,
        LocalDateTime onboardingCompletedAt
) {

    public static OnboardingStatusResponse from(User user) {
        return new OnboardingStatusResponse(
                user.getOnboardingStatus(),
                user.getOnboardingCompletedAt()
        );
    }
}
