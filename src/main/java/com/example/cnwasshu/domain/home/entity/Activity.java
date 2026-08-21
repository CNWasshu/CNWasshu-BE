package com.example.cnwasshu.domain.home.entity;

import com.example.cnwasshu.common.entity.BaseSoftDeleteEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.locationtech.jts.geom.Point;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "activity")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Activity extends BaseSoftDeleteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "activity_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "region_id", nullable = false)
    private Region region;

    @Column(name = "title", nullable = false, length = 100)
    private String title;

    @Column(name = "short_description", length = 255)
    private String shortDescription;

    @Lob
    @Column(name = "description")
    private String description;

    @Column(name = "address", nullable = false, length = 255)
    private String address;

    @Column(name = "location", nullable = false, columnDefinition = "POINT")
    private Point location;

    @Column(name = "phone", length = 30)
    private String phone;

    @Column(name = "operating_start_time")
    private LocalTime operatingStartTime;

    @Column(name = "operating_end_time")
    private LocalTime operatingEndTime;

    @Column(name = "duration")
    private Integer duration;

    @Column(name = "reservation_required")
    private Boolean reservationRequired;

    @Column(name = "today_available")
    private Boolean todayAvailable;

    @Column(name = "thumbnail", length = 500)
    private String thumbnail;

    @Column(name = "qr_code", unique = true, length = 255)
    private String qrCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private ActivityStatus status;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;
}