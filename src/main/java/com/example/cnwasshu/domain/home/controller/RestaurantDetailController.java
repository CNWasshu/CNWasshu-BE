package com.example.cnwasshu.domain.home.controller;

import com.example.cnwasshu.domain.home.dto.RestaurantDetailResponse;
import com.example.cnwasshu.domain.home.service.RestaurantDetailService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/restaurants")
public class RestaurantDetailController {

    private final RestaurantDetailService restaurantDetailService;

    @GetMapping("/{restaurantId}")
    public ResponseEntity<RestaurantDetailResponse> getRestaurantDetail(
            @PathVariable Long restaurantId
    ) {
        return ResponseEntity.ok(
                restaurantDetailService.getRestaurantDetail(restaurantId)
        );
    }
}