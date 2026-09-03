package com.example.cnwasshu.domain.review.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.cnwasshu.common.security.CustomUserPrincipal;
import com.example.cnwasshu.domain.review.dto.response.SurveyDetailResponse;
import com.example.cnwasshu.domain.review.dto.request.SurveyDraftRequest;
import com.example.cnwasshu.domain.review.service.SurveyService;

import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/surveys")
@RequiredArgsConstructor
public class SurveyController {

    private final SurveyService surveyService;

    @GetMapping("/{surveyId}")
    public ResponseEntity<SurveyDetailResponse> getSurvey(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable Long surveyId
    ) {
        return ResponseEntity.ok(surveyService.getSurvey(principal.userId(), surveyId));
    }

    @PatchMapping("/{surveyId}/draft")
    public ResponseEntity<SurveyDetailResponse> saveDraft(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable Long surveyId,
            @Valid @RequestBody SurveyDraftRequest request
    ) {
        return ResponseEntity.ok(surveyService.saveDraft(principal.userId(), surveyId, request));
    }

    @PostMapping("/{surveyId}/submit")
    public ResponseEntity<SurveyDetailResponse> submit(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable Long surveyId
    ) {
        return ResponseEntity.ok(surveyService.submit(principal.userId(), surveyId));
    }
}
