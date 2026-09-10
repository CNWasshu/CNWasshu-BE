package com.example.cnwasshu.domain.notification.controller;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.cnwasshu.common.security.CustomUserPrincipal;
import com.example.cnwasshu.domain.notification.dto.request.NotificationSettingUpdateRequest;
import com.example.cnwasshu.domain.notification.dto.response.NotificationListResponse;
import com.example.cnwasshu.domain.notification.dto.response.NotificationSettingResponse;
import com.example.cnwasshu.domain.notification.service.NotificationService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public ResponseEntity<NotificationListResponse> getMyNotifications(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(notificationService.getMyNotifications(principal.userId(), pageable));
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable Long id
    ) {
        notificationService.markAsRead(principal.userId(), id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/settings")
    public ResponseEntity<NotificationSettingResponse> getMySettings(
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        return ResponseEntity.ok(notificationService.getMySettings(principal.userId()));
    }

    @PatchMapping("/settings")
    public ResponseEntity<NotificationSettingResponse> updateMySettings(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @RequestBody NotificationSettingUpdateRequest request
    ) {
        return ResponseEntity.ok(notificationService.updateMySettings(principal.userId(), request));
    }
}
