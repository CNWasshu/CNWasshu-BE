package com.example.cnwasshu.domain.user.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.cnwasshu.common.security.CustomUserPrincipal;
import com.example.cnwasshu.domain.user.dto.request.DeviceRegisterRequest;
import com.example.cnwasshu.domain.user.dto.request.UserUpdateRequest;
import com.example.cnwasshu.domain.user.dto.response.UserDeviceResponse;
import com.example.cnwasshu.domain.user.dto.response.UserSummary;
import com.example.cnwasshu.domain.user.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<UserSummary> getMe(@AuthenticationPrincipal CustomUserPrincipal principal) {
        return ResponseEntity.ok(userService.getMe(principal.userId()));
    }

    @PatchMapping("/me")
    public ResponseEntity<UserSummary> updateMe(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @Valid @RequestBody UserUpdateRequest request
    ) {
        return ResponseEntity.ok(userService.updateMe(principal.userId(), request));
    }

    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteMe(@AuthenticationPrincipal CustomUserPrincipal principal) {
        userService.deleteMe(principal.userId());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/me/devices")
    public ResponseEntity<UserDeviceResponse> registerDevice(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @Valid @RequestBody DeviceRegisterRequest request
    ) {
        return ResponseEntity.ok(userService.registerDevice(principal.userId(), request));
    }
}
