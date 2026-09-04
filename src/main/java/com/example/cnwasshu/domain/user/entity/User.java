package com.example.cnwasshu.domain.user.entity;

import com.example.cnwasshu.common.entity.BaseSoftDeleteEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;

@Getter
@Entity
@Table(name = "`user`")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseSoftDeleteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    @Column(name = "kakao_id", length = 50, unique = true)
    private String kakaoId;

    @Column(name = "nickname", length = 50, nullable = false)
    private String nickname;

    @Column(name = "email", length = 100)
    private String email;

    // 카카오 로그인 사용자는 null이다. 항상 BCrypt로 인코딩된 값만 저장한다.
    @Column(name = "password", length = 100)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(name = "onboarding_status", length = 20, nullable = false)
    @ColumnDefault("'COMPLETED'")
    private OnboardingStatus onboardingStatus = OnboardingStatus.NOT_STARTED;

    @Column(name = "onboarding_completed_at")
    private LocalDateTime onboardingCompletedAt;

    private User(String kakaoId, String nickname, String email, String password) {
        this.kakaoId = kakaoId;
        this.nickname = nickname;
        this.email = email;
        this.password = password;
    }

    public static User ofKakao(String kakaoId, String nickname, String email) {
        return new User(kakaoId, nickname, email, null);
    }

    public static User ofLocal(String email, String nickname, String encodedPassword) {
        return new User(null, nickname, email, encodedPassword);
    }

    public boolean hasPassword() {
        return password != null;
    }

    public LoginType getLoginType() {
        return kakaoId != null ? LoginType.KAKAO : LoginType.EMAIL;
    }

    public void updateProfile(String nickname, String email) {
        this.nickname = nickname;
        this.email = email;
    }
}
