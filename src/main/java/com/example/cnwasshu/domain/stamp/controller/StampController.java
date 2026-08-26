package com.example.cnwasshu.domain.stamp.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.cnwasshu.common.security.CustomUserPrincipal;
import com.example.cnwasshu.domain.stamp.dto.request.StampCreateRequest;
import com.example.cnwasshu.domain.stamp.dto.response.StampListResponse;
import com.example.cnwasshu.domain.stamp.dto.response.StampResponse;
import com.example.cnwasshu.domain.stamp.service.StampService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/stamps")
@RequiredArgsConstructor
public class StampController {

    private final StampService stampService;

    @PostMapping
    public ResponseEntity<StampResponse> createStamp(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @Valid @RequestBody StampCreateRequest request
    ) {
        StampResponse response = stampService.create(principal.userId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/me")
    public ResponseEntity<StampListResponse> getMyStamps(
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        return ResponseEntity.ok(stampService.getMyStamps(principal.userId()));
    }
}
