package com.example.cnwasshu.domain.home.entity;

import com.example.cnwasshu.common.entity.BaseTimeEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "region")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Region extends BaseTimeEntity {

    @Id
    @Column(name = "region_id")
    private Integer id;

    @Column(name = "region_name", nullable = false, length = 30)
    private String name;

    @Column(name = "is_depopulated_area", nullable = false)
    private Boolean isDepopulatedArea;
}