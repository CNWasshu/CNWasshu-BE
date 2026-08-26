package com.example.cnwasshu.domain.stamp.entity;

import java.time.LocalDateTime;

import com.example.cnwasshu.common.entity.BaseTimeEntity;
import com.example.cnwasshu.domain.home.entity.Activity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "stamp", uniqueConstraints = @UniqueConstraint(
        name = "uk_stamp_user_activity",
        columnNames = {"user_id", "activity_id"}
))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Stamp extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "stamp_id")
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "activity_id", nullable = false)
    private Activity activity;

    @Column(name = "photo", length = 255)
    private String photo;

    @Column(name = "stamped_at", nullable = false)
    private LocalDateTime stampedAt;

    private Stamp(Long userId, Activity activity, String photo) {
        this.userId = userId;
        this.activity = activity;
        this.photo = photo;
        this.stampedAt = LocalDateTime.now();
    }

    public static Stamp of(Long userId, Activity activity, String photo) {
        return new Stamp(userId, activity, photo);
    }
}
