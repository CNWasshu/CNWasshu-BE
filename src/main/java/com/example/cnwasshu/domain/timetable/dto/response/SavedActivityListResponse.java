package com.example.cnwasshu.domain.timetable.dto.response;

import com.example.cnwasshu.domain.timetable.service.SavedActivityInfo;

import java.util.List;

public record SavedActivityListResponse(
        List<SavedActivityResponse> items
) {

    public static SavedActivityListResponse from(List<SavedActivityInfo> activities) {
        return new SavedActivityListResponse(
                activities.stream()
                        .map(SavedActivityResponse::from)
                        .toList()
        );
    }
}
