package com.example.cnwasshu.domain.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.example.cnwasshu.common.exception.BusinessException;
import com.example.cnwasshu.common.exception.ErrorCode;
import com.example.cnwasshu.domain.user.dto.request.OnboardingCompleteRequest;
import com.example.cnwasshu.domain.user.dto.request.OnboardingCompleteRequest.CompletionType;
import com.example.cnwasshu.domain.user.dto.response.OnboardingStatusResponse;
import com.example.cnwasshu.domain.user.entity.OnboardingStatus;
import com.example.cnwasshu.domain.user.entity.User;
import com.example.cnwasshu.domain.user.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OnboardingServiceTest {

    private static final Long USER_ID = 1L;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private OnboardingService onboardingService;

    @Test
    void returnsNotStartedStatusForNewUser() {
        User user = newUser();
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));

        OnboardingStatusResponse response = onboardingService.getStatus(USER_ID);

        assertThat(response.onboardingStatus()).isEqualTo(OnboardingStatus.NOT_STARTED);
        assertThat(response.onboardingCompletedAt()).isNull();
    }

    @Test
    void completesOnboarding() {
        User user = newUser();
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));

        OnboardingStatusResponse response = onboardingService.complete(
                USER_ID,
                new OnboardingCompleteRequest(CompletionType.COMPLETED)
        );

        assertThat(response.onboardingStatus()).isEqualTo(OnboardingStatus.COMPLETED);
        assertThat(response.onboardingCompletedAt()).isNotNull();
    }

    @Test
    void skipsOnboarding() {
        User user = newUser();
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));

        OnboardingStatusResponse response = onboardingService.complete(
                USER_ID,
                new OnboardingCompleteRequest(CompletionType.SKIPPED)
        );

        assertThat(response.onboardingStatus()).isEqualTo(OnboardingStatus.SKIPPED);
        assertThat(response.onboardingCompletedAt()).isNotNull();
    }

    @Test
    void repeatedCompletionKeepsOriginalStatusAndTimestamp() {
        User user = newUser();
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));

        OnboardingStatusResponse firstResponse = onboardingService.complete(
                USER_ID,
                new OnboardingCompleteRequest(CompletionType.COMPLETED)
        );
        LocalDateTime firstCompletedAt = firstResponse.onboardingCompletedAt();

        OnboardingStatusResponse secondResponse = onboardingService.complete(
                USER_ID,
                new OnboardingCompleteRequest(CompletionType.SKIPPED)
        );

        assertThat(secondResponse.onboardingStatus()).isEqualTo(OnboardingStatus.COMPLETED);
        assertThat(secondResponse.onboardingCompletedAt()).isEqualTo(firstCompletedAt);
    }

    @Test
    void rejectsUnknownUser() {
        when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> onboardingService.getStatus(USER_ID))
                .isInstanceOf(BusinessException.class)
                .extracting(exception -> ((BusinessException) exception).getErrorCode())
                .isEqualTo(ErrorCode.USER_NOT_FOUND);
    }

    @Test
    void rejectsDeletedUser() {
        User user = newUser();
        user.softDelete();
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> onboardingService.getStatus(USER_ID))
                .isInstanceOf(BusinessException.class)
                .extracting(exception -> ((BusinessException) exception).getErrorCode())
                .isEqualTo(ErrorCode.USER_NOT_FOUND);
    }

    private User newUser() {
        return User.ofLocal("user@example.com", "충남여행자", "encoded-password");
    }
}
