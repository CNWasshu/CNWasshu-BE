package com.example.cnwasshu.domain.user.controller;

import com.example.cnwasshu.common.security.CustomUserPrincipal;
import com.example.cnwasshu.domain.user.dto.request.OnboardingCompleteRequest;
import com.example.cnwasshu.domain.user.dto.response.OnboardingStatusResponse;
import com.example.cnwasshu.domain.user.service.OnboardingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users/me/onboarding")
@RequiredArgsConstructor
public class OnboardingController {

    private final OnboardingService onboardingService;

    @GetMapping
    public ResponseEntity<OnboardingStatusResponse> getStatus(
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        return ResponseEntity.ok(onboardingService.getStatus(principal.userId()));
    }

    @PatchMapping
    public ResponseEntity<OnboardingStatusResponse> complete(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @Valid @RequestBody OnboardingCompleteRequest request
    ) {
        return ResponseEntity.ok(onboardingService.complete(principal.userId(), request));
    }
}
