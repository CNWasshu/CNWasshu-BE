package com.example.cnwasshu.domain.user.dto.response;

public record TokenResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        long expiresIn,
        boolean isNewUser,
        UserSummary user
) {

    private static final String BEARER = "Bearer";

    public static TokenResponse of(String accessToken, String refreshToken, long expiresIn, boolean isNewUser, UserSummary user) {
        return new TokenResponse(accessToken, refreshToken, BEARER, expiresIn, isNewUser, user);
    }
}
