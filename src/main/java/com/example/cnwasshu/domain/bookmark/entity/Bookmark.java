package com.example.cnwasshu.domain.bookmark.entity;

import com.example.cnwasshu.common.entity.BaseTimeEntity;
import com.example.cnwasshu.domain.home.entity.Activity;
import com.example.cnwasshu.domain.home.entity.Restaurant;
import com.example.cnwasshu.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "bookmark",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_bookmark_user_activity",
                        columnNames = {"user_id", "activity_id"}
                ),
                @UniqueConstraint(
                        name = "uk_bookmark_user_restaurant",
                        columnNames = {"user_id", "restaurant_id"}
                )
        }
)
public class Bookmark extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "bookmark_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "activity_id")
    private Activity activity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id")
    private Restaurant restaurant;

    public Bookmark(User user, Activity activity) {
        this.user = user;
        this.activity = activity;
        this.restaurant = null;
    }

    public Bookmark(User user, Restaurant restaurant) {
        this.user = user;
        this.activity = null;
        this.restaurant = restaurant;
    }
}