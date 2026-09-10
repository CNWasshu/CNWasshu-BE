package com.example.cnwasshu.domain.user.dto.response;

import com.example.cnwasshu.domain.user.dto.kakao.KakaoUserInfoResponse;

public record KakaoUserInfo(
        String kakaoId,
        String nickname,
        String email,
        String profileImage
) {

    public static KakaoUserInfo from(KakaoUserInfoResponse response) {
        KakaoUserInfoResponse.KakaoAccount account = response.kakaoAccount();
        KakaoUserInfoResponse.Profile profile = account != null ? account.profile() : null;

        return new KakaoUserInfo(
                String.valueOf(response.id()),
                profile != null ? profile.nickname() : null,
                account != null ? account.email() : null,
                profile != null ? profile.profileImageUrl() : null
        );
    }
}
