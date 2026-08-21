package com.example.cnwasshu.domain.home.service;

import com.example.cnwasshu.domain.home.dto.HomeItemResponse;
import com.example.cnwasshu.domain.home.dto.HomeItemType;
import com.example.cnwasshu.domain.home.entity.Activity;
import com.example.cnwasshu.domain.home.entity.ActivityTag;
import com.example.cnwasshu.domain.home.entity.ActivityWeather;
import com.example.cnwasshu.domain.home.entity.Restaurant;
import com.example.cnwasshu.domain.home.repository.ActivityRepository;
import com.example.cnwasshu.domain.home.repository.ActivityTagRepository;
import com.example.cnwasshu.domain.home.repository.ActivityWeatherRepository;
import com.example.cnwasshu.domain.home.repository.RestaurantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HomeService {

    private final ActivityRepository activityRepository;
    private final RestaurantRepository restaurantRepository;
    private final ActivityTagRepository activityTagRepository;
    private final ActivityWeatherRepository activityWeatherRepository;

    public List<HomeItemResponse> getHomeItems() {

        List<HomeItemResponse> result = new ArrayList<>();

        List<Activity> activities = activityRepository.findByDeletedAtIsNull();
        List<Restaurant> restaurants = restaurantRepository.findByDeletedAtIsNull();

        for (Activity activity : activities) {
            result.add(toActivityResponse(activity));
        }

        for (Restaurant restaurant : restaurants) {
            result.add(toRestaurantResponse(restaurant));
        }

        return result;
    }

    private HomeItemResponse toActivityResponse(Activity activity) {

        List<String> tags = activityTagRepository
                .findByActivity_Id(activity.getId())
                .stream()
                .map(ActivityTag::getTag)
                .map(tag -> tag.getName())
                .toList();

        List<String> weatherTags = activityWeatherRepository
                .findByActivity_Id(activity.getId())
                .stream()
                .map(ActivityWeather::getWeatherTag)
                .map(weatherTag -> weatherTag.getName())
                .toList();

        return new HomeItemResponse(
                activity.getId(),
                HomeItemType.ACTIVITY,
                activity.getTitle(),
                activity.getShortDescription(),
                activity.getRegion().getId(),
                activity.getRegion().getName(),
                activity.getCategory().getId(),
                activity.getCategory().getName(),
                activity.getThumbnail(),
                activity.getOperatingStartTime(),
                activity.getOperatingEndTime(),
                activity.getReservationRequired(),
                activity.getTodayAvailable(),
                weatherTags,
                tags
        );
    }

    private HomeItemResponse toRestaurantResponse(Restaurant restaurant) {

        return new HomeItemResponse(
                restaurant.getId(),
                HomeItemType.RESTAURANT,
                restaurant.getName(),
                restaurant.getShortDescription(),
                restaurant.getRegion().getId(),
                restaurant.getRegion().getName(),
                restaurant.getCategory().getId(),
                restaurant.getCategory().getName(),
                restaurant.getThumbnail(),
                restaurant.getOperatingStartTime(),
                restaurant.getOperatingEndTime(),
                null,
                null,
                List.of(),
                List.of()
        );
    }
}