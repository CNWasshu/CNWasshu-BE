package com.example.cnwasshu.domain.home.repository;

import com.example.cnwasshu.domain.home.entity.Restaurant;
import com.example.cnwasshu.domain.home.entity.RestaurantStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {

    List<Restaurant> findByStatusAndDeletedAtIsNull(RestaurantStatus status);

    List<Restaurant> findByRegion_IdAndStatusAndDeletedAtIsNull(
            Integer regionId,
            RestaurantStatus status
    );

    List<Restaurant> findByCategory_IdAndStatusAndDeletedAtIsNull(
            Integer categoryId,
            RestaurantStatus status
    );

    List<Restaurant> findByRegion_IdAndCategory_IdAndStatusAndDeletedAtIsNull(
            Integer regionId,
            Integer categoryId,
            RestaurantStatus status
    );

    List<Restaurant> findByDeletedAtIsNull();
}