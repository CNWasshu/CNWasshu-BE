package com.example.cnwasshu.domain.home.repository;

import com.example.cnwasshu.domain.home.entity.RestaurantImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RestaurantImageRepository
        extends JpaRepository<RestaurantImage, Long> {

    List<RestaurantImage> findByRestaurantIdOrderBySortOrderAsc(
            Long restaurantId
    );
}