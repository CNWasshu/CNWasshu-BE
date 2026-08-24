package com.example.cnwasshu.domain.home.service;

import com.example.cnwasshu.domain.home.dto.RestaurantDetailResponse;
import com.example.cnwasshu.domain.home.entity.Restaurant;
import com.example.cnwasshu.domain.home.entity.RestaurantImage;
import com.example.cnwasshu.domain.home.repository.RestaurantImageRepository;
import com.example.cnwasshu.domain.home.repository.RestaurantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RestaurantDetailService {

    private final RestaurantRepository restaurantRepository;
    private final RestaurantImageRepository restaurantImageRepository;

    public RestaurantDetailResponse getRestaurantDetail(Long restaurantId) {

        Restaurant restaurant = restaurantRepository
                .findByIdAndDeletedAtIsNull(restaurantId)
                .orElseThrow(() ->
                        new IllegalArgumentException("존재하지 않는 맛집입니다.")
                );

        List<String> images = restaurantImageRepository
                .findByRestaurantIdOrderBySortOrderAsc(restaurantId)
                .stream()
                .map(RestaurantImage::getImageUrl)
                .toList();

        return RestaurantDetailResponse.from(
                restaurant,
                images
        );
    }
}