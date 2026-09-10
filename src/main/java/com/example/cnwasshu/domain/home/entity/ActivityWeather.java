package com.example.cnwasshu.domain.home.entity;

import com.example.cnwasshu.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "activity_weather")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ActivityWeather extends BaseTimeEntity {

    @EmbeddedId
    private ActivityWeatherId id;

    @MapsId("activityId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "activity_id")
    private Activity activity;

    @MapsId("weatherId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "weather_id")
    private WeatherTag weatherTag;
}