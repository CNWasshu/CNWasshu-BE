package com.example.cnwasshu.domain.stamp.dto.response;

import java.util.List;

import com.example.cnwasshu.domain.stamp.entity.Stamp;

public record StampListResponse(
        List<StampResponse> stamps
) {

    public static StampListResponse from(List<Stamp> stampList) {
        return new StampListResponse(stampList.stream().map(StampResponse::from).toList());
    }
}
