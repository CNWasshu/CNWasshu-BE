package com.example.cnwasshu.domain.home.dto;

import java.util.List;

public record HomePageResponse(
        List<HomeItemResponse> items,
        int page,
        int size,
        boolean hasNext,
        long totalElements,
        int totalPages
) {
}