package com.example.cnwasshu.domain.user.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.Optional;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.cnwasshu.common.exception.BusinessException;
import com.example.cnwasshu.common.exception.ErrorCode;
import com.example.cnwasshu.common.security.jwt.JwtTokenProvider;
import com.example.cnwasshu.domain.user.dto.request.KakaoLoginRequest;
import com.example.cnwasshu.domain.user.dto.request.LoginRequest;
import com.example.cnwasshu.domain.user.dto.request.SignupRequest;
import com.example.cnwasshu.domain.user.dto.response.KakaoUserInfo;
import com.example.cnwasshu.domain.user.dto.response.TokenResponse;
import com.example.cnwasshu.domain.user.dto.response.UserSummary;
import com.example.cnwasshu.domain.user.entity.RefreshToken;
import com.example.cnwasshu.domain.user.entity.User;
import com.example.cnwasshu.domain.user.repository.RefreshTokenRepository;
import com.example.cnwasshu.domain.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {

    private static final String DEFAULT_NICKNAME_PREFIX = "카카오사용자";

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final KakaoOAuthClient kakaoOAuthClient;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;

    public TokenResponse signup(SignupRequest request) {
        userRepository.findByEmail(request.email())
                .ifPresent(user -> {
                    throw new BusinessException(ErrorCode.EMAIL_ALREADY_EXISTS);
                });

        User user = userRepository.save(User.ofLocal(
                request.email(),
                request.nickname(),
                passwordEncoder.encode(request.password())
        ));

        return issueTokens(user, true, false);
    }

    public TokenResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .filter(User::hasPassword)
                .filter(candidate -> passwordEncoder.matches(request.password(), candidate.getPassword()))
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_CREDENTIALS));

        // 소프트 삭제(탈퇴)했던 계정이 같은 이메일로 재로그인하면 탈퇴를 취소하고 정상 로그인 처리한다.
        if (user.isDeleted()) {
            user.restore();
        }

        return issueTokens(user, false, false);
    }

    public TokenResponse loginWithKakao(KakaoLoginRequest request) {
        String kakaoAccessToken = resolveKakaoAccessToken(request);
        KakaoUserInfo kakaoUserInfo = kakaoOAuthClient.fetchUserInfo(kakaoAccessToken);

        Optional<User> existingUser = userRepository.findByKakaoId(kakaoUserInfo.kakaoId());
        boolean isNewUser = existingUser.isEmpty();
        // 카카오 닉네임 동의항목은 "선택 동의"라 사용자가 거부하면 nickname이 안 온다.
        // fallback으로 치환하기 전, 원본 응답 시점에 직접 판단한다
        // (저장된 닉네임 문자열의 접두사를 나중에 패턴 매칭하는 방식은 오탐 가능성이 있어 쓰지 않는다).
        boolean nicknameNeedsSetup = isNewUser
                && (kakaoUserInfo.nickname() == null || kakaoUserInfo.nickname().isBlank());
        // 프로필 사진은 전 유저 공통 마스코트 이미지로 고정하기로 해서, 카카오가 줘도 저장하지 않는다.
        User user = existingUser.orElseGet(() -> userRepository.save(User.ofKakao(
                kakaoUserInfo.kakaoId(),
                resolveNickname(kakaoUserInfo.nickname()),
                kakaoUserInfo.email()
        )));

        // 소프트 삭제(탈퇴)했던 계정이 같은 kakao_id로 재로그인하면 탈퇴를 취소하고 정상 로그인 처리한다.
        if (user.isDeleted()) {
            user.restore();
        }

        return issueTokens(user, isNewUser, nicknameNeedsSetup);
    }

    public TokenResponse reissue(String refreshToken) {
        jwtTokenProvider.validateRefreshToken(refreshToken);
        Long userId = jwtTokenProvider.getUserId(refreshToken);

        RefreshToken savedToken = refreshTokenRepository.findByTokenHash(hash(refreshToken))
                .filter(token -> token.getUser().getId().equals(userId))
                .orElseThrow(() -> new BusinessException(ErrorCode.REFRESH_TOKEN_NOT_FOUND));
        refreshTokenRepository.delete(savedToken);

        if (savedToken.isExpired()) {
            throw new BusinessException(ErrorCode.REFRESH_TOKEN_NOT_FOUND);
        }

        return issueTokens(savedToken.getUser(), false, false);
    }

    public void logout(Long userId, String refreshToken) {
        refreshTokenRepository.findByTokenHash(hash(refreshToken))
                .filter(token -> token.getUser().getId().equals(userId))
                .ifPresent(refreshTokenRepository::delete);
    }

    private TokenResponse issueTokens(User user, boolean isNewUser, boolean nicknameNeedsSetup) {
        String accessToken = jwtTokenProvider.generateAccessToken(user.getId());
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId());

        LocalDateTime expiresAt = LocalDateTime.now().plusSeconds(jwtTokenProvider.getRefreshTokenValiditySeconds());
        refreshTokenRepository.save(RefreshToken.of(user, hash(refreshToken), expiresAt));

        return TokenResponse.of(
                accessToken,
                refreshToken,
                jwtTokenProvider.getAccessTokenValiditySeconds(),
                isNewUser,
                nicknameNeedsSetup,
                UserSummary.from(user)
        );
    }

    private String resolveKakaoAccessToken(KakaoLoginRequest request) {
        if (request.hasAccessToken()) {
            return request.accessToken();
        }
        if (request.hasAuthorizationCode()) {
            return kakaoOAuthClient.exchangeToken(request.authorizationCode(), request.redirectUri());
        }
        throw new BusinessException(ErrorCode.INVALID_KAKAO_LOGIN_REQUEST);
    }

    private String resolveNickname(String nickname) {
        if (nickname != null && !nickname.isBlank()) {
            return nickname;
        }
        return DEFAULT_NICKNAME_PREFIX + UUID.randomUUID().toString().substring(0, 8);
    }

    private String hash(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashed);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm not available", e);
        }
    }
}
