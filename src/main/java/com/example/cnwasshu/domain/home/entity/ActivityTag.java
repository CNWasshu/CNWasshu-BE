package com.example.cnwasshu.domain.home.entity;

import com.example.cnwasshu.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "activity_tag")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ActivityTag extends BaseTimeEntity {

    @EmbeddedId
    private ActivityTagId id;

    @MapsId("activityId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "activity_id")
    private Activity activity;

    @MapsId("tagId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tag_id")
    private Tag tag;
}