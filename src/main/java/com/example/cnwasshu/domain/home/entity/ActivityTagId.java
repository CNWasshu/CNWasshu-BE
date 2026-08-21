package com.example.cnwasshu.domain.home.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@EqualsAndHashCode
public class ActivityTagId implements Serializable {

    @Column(name = "activity_id")
    private Long activityId;

    @Column(name = "tag_id")
    private Integer tagId;
}