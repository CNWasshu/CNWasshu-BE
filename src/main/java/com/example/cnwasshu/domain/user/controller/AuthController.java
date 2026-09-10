package com.example.cnwasshu.domain.user.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.cnwasshu.common.security.CustomUserPrincipal;
import com.example.cnwasshu.domain.user.dto.request.KakaoLoginRequest;
import com.example.cnwasshu.domain.user.dto.request.LoginRequest;
import com.example.cnwasshu.domain.user.dto.request.SignupRequest;
import com.example.cnwasshu.domain.user.dto.request.TokenRefreshRequest;
import com.example.cnwasshu.domain.user.dto.response.TokenResponse;
import com.example.cnwasshu.domain.user.service.AuthService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/kakao")
    public ResponseEntity<TokenResponse> kakaoLogin(@RequestBody KakaoLoginRequest request) {
        return ResponseEntity.ok(authService.loginWithKakao(request));
    }

    @PostMapping("/signup")
    public ResponseEntity<TokenResponse> signup(@Valid @RequestBody SignupRequest request) {
        return ResponseEntity.ok(authService.signup(request));
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refresh(@Valid @RequestBody TokenRefreshRequest request) {
        return ResponseEntity.ok(authService.reissue(request.refreshToken()));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @Valid @RequestBody TokenRefreshRequest request
    ) {
        authService.logout(principal.userId(), request.refreshToken());
        return ResponseEntity.noContent().build();
    }
}
