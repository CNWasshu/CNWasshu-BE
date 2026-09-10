package com.example.cnwasshu.domain.home.dto;

import java.time.LocalTime;
import java.util.List;

public record HomeItemResponse(
        Long id,
        HomeItemType type,
        String title,
        String shortDescription,
        Integer regionId,
        String regionName,
        Integer categoryId,
        String categoryName,
        String thumbnail,
        LocalTime operatingStartTime,
        LocalTime operatingEndTime,
        Integer maxParticipants,
        Boolean reservationRequired,
        Boolean todayAvailable,
        List<String> weatherTags,
        List<String> tags
) {
}