package com.example.cnwasshu.domain.home.dto;

import com.example.cnwasshu.domain.home.entity.Restaurant;
import com.example.cnwasshu.domain.home.entity.RestaurantStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalTime;
import java.util.List;

@Getter
@Builder
public class RestaurantDetailResponse {

    private Long id;

    private String name;
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

    private String thumbnail;

    private RestaurantStatus status;

    private List<String> images;

    public static RestaurantDetailResponse from(
            Restaurant restaurant,
            List<String> images
    ) {
        return RestaurantDetailResponse.builder()
                .id(restaurant.getId())

                .name(restaurant.getName())
                .shortDescription(restaurant.getShortDescription())
                .description(restaurant.getDescription())

                .regionId(restaurant.getRegion().getId())
                .regionName(restaurant.getRegion().getName())

                .categoryId(restaurant.getCategory().getId())
                .categoryName(restaurant.getCategory().getName())

                .address(restaurant.getAddress())
                .longitude(
                        restaurant.getLocation() != null
                                ? restaurant.getLocation().getX()
                                : null
                )
                .latitude(
                        restaurant.getLocation() != null
                                ? restaurant.getLocation().getY()
                                : null
                )
                .phone(restaurant.getPhone())

                .operatingStartTime(restaurant.getOperatingStartTime())
                .operatingEndTime(restaurant.getOperatingEndTime())

                .thumbnail(restaurant.getThumbnail())

                .status(restaurant.getStatus())

                .images(images)
                .build();
    }
}