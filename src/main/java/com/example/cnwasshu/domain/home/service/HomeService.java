package com.example.cnwasshu.domain.home.service;

import com.example.cnwasshu.domain.home.dto.ActivityHomeSort;
import com.example.cnwasshu.domain.home.dto.HomeItemResponse;
import com.example.cnwasshu.domain.home.dto.HomeItemType;
import com.example.cnwasshu.domain.home.dto.HomePageResponse;
import com.example.cnwasshu.domain.home.dto.RestaurantHomeSort;
import com.example.cnwasshu.domain.home.entity.Activity;
import com.example.cnwasshu.domain.home.entity.ActivityTag;
import com.example.cnwasshu.domain.home.entity.ActivityWeather;
import com.example.cnwasshu.domain.home.entity.Restaurant;
import com.example.cnwasshu.domain.home.repository.ActivityRepository;
import com.example.cnwasshu.domain.home.repository.ActivityTagRepository;
import com.example.cnwasshu.domain.home.repository.ActivityWeatherRepository;
import com.example.cnwasshu.domain.home.repository.RestaurantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    public HomePageResponse getActivities(
            ActivityHomeSort sort,
            int page,
            int size
    ) {
        Pageable pageable = PageRequest.of(page, size);

        Page<Long> activityIdPage = switch (sort) {
            case DEFAULT ->
                    activityRepository.findHomeActivityIdsDefault(pageable);
            case RECOMMENDED ->
                    activityRepository.findHomeActivityIdsRecommended(pageable);
            case RESERVATION ->
                    activityRepository.findHomeActivityIdsReservation(pageable);
            case BOOKMARK ->
                    activityRepository.findHomeActivityIdsBookmark(pageable);
        };

        List<Long> ids = activityIdPage.getContent();

        if (ids.isEmpty()) {
            return new HomePageResponse(
                    List.of(),
                    activityIdPage.getNumber(),
                    activityIdPage.getSize(),
                    activityIdPage.hasNext(),
                    activityIdPage.getTotalElements(),
                    activityIdPage.getTotalPages()
            );
        }

        List<Activity> activities =
                activityRepository.findAllByIdsWithRegionAndCategory(ids);

        Map<Long, Activity> activityMap = new HashMap<>();

        for (Activity activity : activities) {
            activityMap.put(activity.getId(), activity);
        }

        List<HomeItemResponse> items = new ArrayList<>();

        for (Long id : ids) {
            Activity activity = activityMap.get(id);

            if (activity != null) {
                items.add(toActivityResponse(activity));
            }
        }

        return new HomePageResponse(
                items,
                activityIdPage.getNumber(),
                activityIdPage.getSize(),
                activityIdPage.hasNext(),
                activityIdPage.getTotalElements(),
                activityIdPage.getTotalPages()
        );
    }

    public HomePageResponse getRestaurants(
            RestaurantHomeSort sort,
            int page,
            int size
    ) {
        Pageable pageable = PageRequest.of(page, size);

        Page<Long> restaurantIdPage = switch (sort) {
            case NAME ->
                    restaurantRepository.findHomeRestaurantIdsName(pageable);
            case BOOKMARK ->
                    restaurantRepository.findHomeRestaurantIdsBookmark(pageable);
        };

        List<Long> ids = restaurantIdPage.getContent();

        if (ids.isEmpty()) {
            return new HomePageResponse(
                    List.of(),
                    restaurantIdPage.getNumber(),
                    restaurantIdPage.getSize(),
                    restaurantIdPage.hasNext(),
                    restaurantIdPage.getTotalElements(),
                    restaurantIdPage.getTotalPages()
            );
        }

        List<Restaurant> restaurants =
                restaurantRepository.findAllByIdsWithRegionAndCategory(ids);

        Map<Long, Restaurant> restaurantMap = new HashMap<>();

        for (Restaurant restaurant : restaurants) {
            restaurantMap.put(restaurant.getId(), restaurant);
        }

        List<HomeItemResponse> items = new ArrayList<>();

        for (Long id : ids) {
            Restaurant restaurant = restaurantMap.get(id);

            if (restaurant != null) {
                items.add(toRestaurantResponse(restaurant));
            }
        }

        return new HomePageResponse(
                items,
                restaurantIdPage.getNumber(),
                restaurantIdPage.getSize(),
                restaurantIdPage.hasNext(),
                restaurantIdPage.getTotalElements(),
                restaurantIdPage.getTotalPages()
        );
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
                activity.getMaxParticipants(),
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
                null,
                List.of(),
                List.of()
        );
    }
}