package com.example.cnwasshu.domain.bookmark.dto;

import com.example.cnwasshu.domain.bookmark.entity.BookmarkType;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class BookmarkRequest {

    private BookmarkType type;
    private Long targetId;
}