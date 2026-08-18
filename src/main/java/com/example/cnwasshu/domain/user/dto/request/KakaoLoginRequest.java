package com.example.cnwasshu.domain.user.dto.request;

public record KakaoLoginRequest(
        String authorizationCode,
        String redirectUri,
        String accessToken
) {

    public boolean hasAccessToken() {
        return accessToken != null && !accessToken.isBlank();
    }

    public boolean hasAuthorizationCode() {
        return authorizationCode != null && !authorizationCode.isBlank();
    }
}
