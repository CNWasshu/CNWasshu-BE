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
public class ActivityWeatherId implements Serializable {

    @Column(name = "activity_id")
    private Long activityId;

    @Column(name = "weather_id")
    private Integer weatherId;
}