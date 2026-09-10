package com.example.cnwasshu.domain.bookmark.controller;

import com.example.cnwasshu.common.security.CustomUserPrincipal;
import com.example.cnwasshu.domain.bookmark.dto.BookmarkRequest;
import com.example.cnwasshu.domain.bookmark.dto.BookmarkResponse;
import com.example.cnwasshu.domain.bookmark.service.BookmarkService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/bookmarks")
public class BookmarkController {

    private final BookmarkService bookmarkService;

    @PostMapping
    public ResponseEntity<Void> addBookmark(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @RequestBody BookmarkRequest request
    ) {
        bookmarkService.addBookmark(
                principal.userId(),
                request.getType(),
                request.getTargetId()
        );

        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<List<BookmarkResponse>> getBookmarks(
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        return ResponseEntity.ok(
                bookmarkService.getBookmarks(principal.userId())
        );
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteBookmark(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @RequestBody BookmarkRequest request
    ) {
        bookmarkService.deleteBookmark(
                principal.userId(),
                request.getType(),
                request.getTargetId()
        );

        return ResponseEntity.noContent().build();
    }
}