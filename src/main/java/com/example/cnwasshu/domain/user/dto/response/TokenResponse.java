package com.example.cnwasshu.domain.user.dto.response;

public record TokenResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        long expiresIn,
        boolean isNewUser,
        // 카카오 닉네임 동의항목이 "선택 동의"라 카카오가 닉네임을 안 줄 수 있다.
        // 신규 가입자인데 카카오가 준 닉네임이 없어서(자동생성 값으로 대체) 온보딩에서
        // 직접 입력받아야 하는 경우에만 true. 이메일 회원가입/기존 유저 로그인은 항상 false.
        boolean nicknameNeedsSetup,
        UserSummary user
) {

    private static final String BEARER = "Bearer";

    public static TokenResponse of(
            String accessToken,
            String refreshToken,
            long expiresIn,
            boolean isNewUser,
            boolean nicknameNeedsSetup,
            UserSummary user
    ) {
        return new TokenResponse(accessToken, refreshToken, BEARER, expiresIn, isNewUser, nicknameNeedsSetup, user);
    }
}
