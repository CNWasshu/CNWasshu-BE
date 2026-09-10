package com.example.cnwasshu.domain.user.service;

import com.example.cnwasshu.common.exception.BusinessException;
import com.example.cnwasshu.common.exception.ErrorCode;
import com.example.cnwasshu.domain.user.dto.request.OnboardingCompleteRequest;
import com.example.cnwasshu.domain.user.dto.response.OnboardingStatusResponse;
import com.example.cnwasshu.domain.user.entity.OnboardingStatus;
import com.example.cnwasshu.domain.user.entity.User;
import com.example.cnwasshu.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OnboardingService {

    private final UserRepository userRepository;

    public OnboardingStatusResponse getStatus(Long userId) {
        return OnboardingStatusResponse.from(getActiveUser(userId));
    }

    @Transactional
    public OnboardingStatusResponse complete(Long userId, OnboardingCompleteRequest request) {
        User user = getActiveUser(userId);
        OnboardingStatus completionStatus = OnboardingStatus.valueOf(request.completionType().name());

        user.completeOnboarding(completionStatus);
        return OnboardingStatusResponse.from(user);
    }

    private User getActiveUser(Long userId) {
        return userRepository.findById(userId)
                .filter(user -> !user.isDeleted())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }
}
