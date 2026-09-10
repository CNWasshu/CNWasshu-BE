package com.example.cnwasshu.domain.home.entity;

import com.example.cnwasshu.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "weather_tag")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WeatherTag extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "weather_id")
    private Integer id;

    @Column(name = "weather_name", nullable = false, length = 30)
    private String name;
}