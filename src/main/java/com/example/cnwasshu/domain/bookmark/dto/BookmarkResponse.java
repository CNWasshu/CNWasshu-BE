package com.example.cnwasshu.domain.bookmark.dto;

import com.example.cnwasshu.domain.bookmark.entity.Bookmark;
import com.example.cnwasshu.domain.bookmark.entity.BookmarkType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class BookmarkResponse {

    private Long bookmarkId;
    private BookmarkType type;
    private Long targetId;
    private String title;
    private String thumbnail;
    private LocalDateTime createdAt;

    public static BookmarkResponse from(Bookmark bookmark) {

        if (bookmark.getActivity() != null) {
            return BookmarkResponse.builder()
                    .bookmarkId(bookmark.getId())
                    .type(BookmarkType.ACTIVITY)
                    .targetId(bookmark.getActivity().getId())
                    .title(bookmark.getActivity().getTitle())
                    .thumbnail(bookmark.getActivity().getThumbnail())
                    .createdAt(bookmark.getCreatedAt())
                    .build();
        }

        return BookmarkResponse.builder()
                .bookmarkId(bookmark.getId())
                .type(BookmarkType.RESTAURANT)
                .targetId(bookmark.getRestaurant().getId())
                .title(bookmark.getRestaurant().getName())
                .thumbnail(bookmark.getRestaurant().getThumbnail())
                .createdAt(bookmark.getCreatedAt())
                .build();
    }
}