package com.example.cnwasshu.domain.home.dto;

import com.example.cnwasshu.domain.home.entity.Activity;
import com.example.cnwasshu.domain.home.entity.ActivityStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Getter
@Builder
public class ActivityDetailResponse {

    private Long id;

    private String title;
    private String shortDescription;
    private String description;

    private Integer regionId;
    private String regionName;

    private Integer categoryId;
    private String categoryName;

    private String address;
    private Double longitude;
    private Double latitude;
    private String phone;

    private LocalTime operatingStartTime;
    private LocalTime operatingEndTime;
    private Integer duration;

    private Boolean reservationRequired;
    private Integer maxParticipants;
    private Boolean todayAvailable;

    private String thumbnail;

    private ActivityStatus status;
    private LocalDate startDate;
    private LocalDate endDate;

    private List<String> images;

    public static ActivityDetailResponse from(
            Activity activity,
            List<String> images
    ) {
        return ActivityDetailResponse.builder()
                .id(activity.getId())

                .title(activity.getTitle())
                .shortDescription(activity.getShortDescription())
                .description(activity.getDescription())

                .regionId(activity.getRegion().getId())
                .regionName(activity.getRegion().getName())

                .categoryId(activity.getCategory().getId())
                .categoryName(activity.getCategory().getName())

                .address(activity.getAddress())
                .longitude(
                        activity.getLocation() != null
                                ? activity.getLocation().getX()
                                : null
                )
                .latitude(
                        activity.getLocation() != null
                                ? activity.getLocation().getY()
                                : null
                )
                .phone(activity.getPhone())

                .operatingStartTime(activity.getOperatingStartTime())
                .operatingEndTime(activity.getOperatingEndTime())
                .duration(activity.getDuration())

                .reservationRequired(activity.getReservationRequired())
                .maxParticipants(activity.getMaxParticipants())
                .todayAvailable(activity.getTodayAvailable())

                .thumbnail(activity.getThumbnail())

                .status(activity.getStatus())
                .startDate(activity.getStartDate())
                .endDate(activity.getEndDate())

                .images(images)
                .build();
    }
}