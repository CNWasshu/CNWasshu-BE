package com.example.cnwasshu.domain.stamp.dto.response;

import java.time.LocalDateTime;

import com.example.cnwasshu.domain.stamp.entity.Stamp;

public record StampResponse(
        Long stampId,
        Long activityId,
        String activityTitle,
        String regionName,
        String photo,
        LocalDateTime stampedAt
) {

    public static StampResponse from(Stamp stamp) {
        return new StampResponse(
                stamp.getId(),
                stamp.getActivity().getId(),
                stamp.getActivity().getTitle(),
                stamp.getActivity().getRegion().getName(),
                stamp.getPhoto(),
                stamp.getStampedAt()
        );
    }
}
