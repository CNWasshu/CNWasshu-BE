package com.example.cnwasshu.domain.home.controller;

import com.example.cnwasshu.domain.home.dto.ActivityHomeSort;
import com.example.cnwasshu.domain.home.dto.HomeFilterOptionResponse;
import com.example.cnwasshu.domain.home.dto.HomeItemResponse;
import com.example.cnwasshu.domain.home.dto.HomeItemType;
import com.example.cnwasshu.domain.home.dto.HomePageResponse;
import com.example.cnwasshu.domain.home.dto.RestaurantHomeSort;
import com.example.cnwasshu.domain.home.service.HomeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/home")
public class HomeController {

    private final HomeService homeService;

    @GetMapping
    public ResponseEntity<List<HomeItemResponse>> getHomeItems() {
        return ResponseEntity.ok(
                homeService.getHomeItems()
        );
    }

    @GetMapping("/activities")
    public ResponseEntity<HomePageResponse> getActivities(
            @RequestParam(defaultValue = "DEFAULT")
            ActivityHomeSort sort,
            @RequestParam(required = false)
            Integer regionId,
            @RequestParam(required = false)
            Integer categoryId,
            @RequestParam(defaultValue = "0")
            int page,
            @RequestParam(defaultValue = "8")
            int size
    ) {
        return ResponseEntity.ok(
                homeService.getActivities(
                        sort,
                        regionId,
                        categoryId,
                        page,
                        size
                )
        );
    }

    @GetMapping("/restaurants")
    public ResponseEntity<HomePageResponse> getRestaurants(
            @RequestParam(defaultValue = "NAME")
            RestaurantHomeSort sort,
            @RequestParam(required = false)
            Integer regionId,
            @RequestParam(required = false)
            Integer categoryId,
            @RequestParam(defaultValue = "0")
            int page,
            @RequestParam(defaultValue = "8")
            int size
    ) {
        return ResponseEntity.ok(
                homeService.getRestaurants(
                        sort,
                        regionId,
                        categoryId,
                        page,
                        size
                )
        );
    }

    @GetMapping("/filters")
    public ResponseEntity<HomeFilterOptionResponse> getFilterOptions(
            @RequestParam
            HomeItemType type
    ) {
        return ResponseEntity.ok(
                homeService.getFilterOptions(type)
        );
    }
}