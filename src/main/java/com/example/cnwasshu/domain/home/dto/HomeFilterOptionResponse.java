package com.example.cnwasshu.domain.home.dto;

import java.util.List;

public record HomeFilterOptionResponse(
        List<HomeFilterOption> regions,
        List<HomeFilterOption> categories
) {
}