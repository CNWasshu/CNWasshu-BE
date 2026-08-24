package com.example.cnwasshu.domain.bookmark.dto;

import com.example.cnwasshu.domain.bookmark.entity.Bookmark;
import com.example.cnwasshu.domain.bookmark.entity.BookmarkType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Getter
@Builder
public class BookmarkResponse {

    private Long bookmarkId;

    private BookmarkType type;

    private Long targetId;

    private String title;

    private String thumbnail;

    private String regionName;

    private String categoryName;

    private LocalTime operatingStartTime;

    private LocalTime operatingEndTime;

    private Boolean reservationRequired;

    private LocalDateTime createdAt;

    public static BookmarkResponse from(Bookmark bookmark) {

        if (bookmark.getActivity() != null) {
            return BookmarkResponse.builder()
                    .bookmarkId(bookmark.getId())
                    .type(BookmarkType.ACTIVITY)
                    .targetId(bookmark.getActivity().getId())
                    .title(bookmark.getActivity().getTitle())
                    .thumbnail(bookmark.getActivity().getThumbnail())
                    .regionName(
                            bookmark.getActivity()
                                    .getRegion()
                                    .getName()
                    )
                    .categoryName(
                            bookmark.getActivity()
                                    .getCategory()
                                    .getName()
                    )
                    .operatingStartTime(
                            bookmark.getActivity().getOperatingStartTime()
                    )
                    .operatingEndTime(
                            bookmark.getActivity().getOperatingEndTime()
                    )
                    .reservationRequired(
                            bookmark.getActivity().getReservationRequired()
                    )
                    .createdAt(bookmark.getCreatedAt())
                    .build();
        }

        return BookmarkResponse.builder()
                .bookmarkId(bookmark.getId())
                .type(BookmarkType.RESTAURANT)
                .targetId(bookmark.getRestaurant().getId())
                .title(bookmark.getRestaurant().getName())
                .thumbnail(bookmark.getRestaurant().getThumbnail())
                .regionName(
                        bookmark.getRestaurant()
                                .getRegion()
                                .getName()
                )
                .categoryName(
                        bookmark.getRestaurant()
                                .getCategory()
                                .getName()
                )
                .operatingStartTime(
                        bookmark.getRestaurant().getOperatingStartTime()
                )
                .operatingEndTime(
                        bookmark.getRestaurant().getOperatingEndTime()
                )
                .reservationRequired(null)
                .createdAt(bookmark.getCreatedAt())
                .build();
    }
}