package com.example.cnwasshu.domain.user.dto.response;

import com.example.cnwasshu.domain.user.entity.User;

public record UserSummary(
        Long userId,
        String nickname,
        String email,
        String profileImage
) {

    public static UserSummary from(User user) {
        return new UserSummary(user.getId(), user.getNickname(), user.getEmail(), user.getProfileImage());
    }
}
