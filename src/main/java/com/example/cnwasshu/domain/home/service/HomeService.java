package com.example.cnwasshu.domain.home.service;

import com.example.cnwasshu.domain.home.dto.ActivityHomeSort;
import com.example.cnwasshu.domain.home.dto.HomeFilterOption;
import com.example.cnwasshu.domain.home.dto.HomeFilterOptionResponse;
import com.example.cnwasshu.domain.home.dto.HomeItemResponse;
import com.example.cnwasshu.domain.home.dto.HomeItemType;
import com.example.cnwasshu.domain.home.dto.HomePageResponse;
import com.example.cnwasshu.domain.home.dto.RestaurantHomeSort;
import com.example.cnwasshu.domain.home.entity.Activity;
import com.example.cnwasshu.domain.home.entity.ActivityStatus;
import com.example.cnwasshu.domain.home.entity.ActivityTag;
import com.example.cnwasshu.domain.home.entity.ActivityWeather;
import com.example.cnwasshu.domain.home.entity.Restaurant;
import com.example.cnwasshu.domain.home.entity.RestaurantStatus;
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

        List<Activity> activities =
                activityRepository.findByDeletedAtIsNull();

        List<Restaurant> restaurants =
                restaurantRepository.findByDeletedAtIsNull();

        List<Long> activityIds = activities.stream()
                .map(Activity::getId)
                .toList();

        Map<Long, List<String>> tagMap =
                getTagMap(activityIds);

        Map<Long, List<String>> weatherTagMap =
                getWeatherTagMap(activityIds);

        for (Activity activity : activities) {
            result.add(
                    toActivityResponse(
                            activity,
                            tagMap.getOrDefault(
                                    activity.getId(),
                                    List.of()
                            ),
                            weatherTagMap.getOrDefault(
                                    activity.getId(),
                                    List.of()
                            )
                    )
            );
        }

        for (Restaurant restaurant : restaurants) {
            result.add(toRestaurantResponse(restaurant));
        }

        return result;
    }

    public HomePageResponse getActivities(
            ActivityHomeSort sort,
            Integer regionId,
            Integer categoryId,
            String keyword,
            int page,
            int size
    ) {
        Pageable pageable = PageRequest.of(page, size);

        String normalizedKeyword =
                normalizeKeyword(keyword);

        Page<Long> activityIdPage = switch (sort) {
            case DEFAULT ->
                    activityRepository.findHomeActivityIdsDefault(
                            regionId,
                            categoryId,
                            normalizedKeyword,
                            pageable
                    );

            case RECOMMENDED ->
                    activityRepository.findHomeActivityIdsRecommended(
                            regionId,
                            categoryId,
                            normalizedKeyword,
                            pageable
                    );

            case RESERVATION ->
                    activityRepository.findHomeActivityIdsReservation(
                            regionId,
                            categoryId,
                            normalizedKeyword,
                            pageable
                    );

            case BOOKMARK ->
                    activityRepository.findHomeActivityIdsBookmark(
                            regionId,
                            categoryId,
                            normalizedKeyword,
                            pageable
                    );
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
            activityMap.put(
                    activity.getId(),
                    activity
            );
        }

        Map<Long, List<String>> tagMap =
                getTagMap(ids);

        Map<Long, List<String>> weatherTagMap =
                getWeatherTagMap(ids);

        List<HomeItemResponse> items = new ArrayList<>();

        for (Long id : ids) {
            Activity activity = activityMap.get(id);

            if (activity != null) {
                items.add(
                        toActivityResponse(
                                activity,
                                tagMap.getOrDefault(
                                        id,
                                        List.of()
                                ),
                                weatherTagMap.getOrDefault(
                                        id,
                                        List.of()
                                )
                        )
                );
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
            Integer regionId,
            Integer categoryId,
            String keyword,
            int page,
            int size
    ) {
        Pageable pageable = PageRequest.of(page, size);

        String normalizedKeyword =
                normalizeKeyword(keyword);

        Page<Long> restaurantIdPage =
                switch (sort) {
                    case NAME ->
                            restaurantRepository
                                    .findHomeRestaurantIdsName(
                                            regionId,
                                            categoryId,
                                            normalizedKeyword,
                                            pageable
                                    );

                    case BOOKMARK ->
                            restaurantRepository
                                    .findHomeRestaurantIdsBookmark(
                                            regionId,
                                            categoryId,
                                            normalizedKeyword,
                                            pageable
                                    );
                };

        List<Long> ids =
                restaurantIdPage.getContent();

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
                restaurantRepository
                        .findAllByIdsWithRegionAndCategory(ids);

        Map<Long, Restaurant> restaurantMap =
                new HashMap<>();

        for (Restaurant restaurant : restaurants) {
            restaurantMap.put(
                    restaurant.getId(),
                    restaurant
            );
        }

        List<HomeItemResponse> items =
                new ArrayList<>();

        for (Long id : ids) {
            Restaurant restaurant =
                    restaurantMap.get(id);

            if (restaurant != null) {
                items.add(
                        toRestaurantResponse(restaurant)
                );
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

    public HomeFilterOptionResponse getFilterOptions(
            HomeItemType type
    ) {
        if (type == HomeItemType.ACTIVITY) {

            List<HomeFilterOption> regions =
                    mapFilterOptions(
                            activityRepository.findHomeRegionOptions(
                                    ActivityStatus.OPEN
                            )
                    );

            List<HomeFilterOption> categories =
                    mapFilterOptions(
                            activityRepository.findHomeCategoryOptions(
                                    ActivityStatus.OPEN
                            )
                    );

            return new HomeFilterOptionResponse(
                    regions,
                    categories
            );
        }

        List<HomeFilterOption> regions =
                mapFilterOptions(
                        restaurantRepository.findHomeRegionOptions(
                                RestaurantStatus.OPEN
                        )
                );

        List<HomeFilterOption> categories =
                mapFilterOptions(
                        restaurantRepository.findHomeCategoryOptions(
                                RestaurantStatus.OPEN
                        )
                );

        return new HomeFilterOptionResponse(
                regions,
                categories
        );
    }

    private String normalizeKeyword(
            String keyword
    ) {
        if (keyword == null) {
            return null;
        }

        String trimmedKeyword = keyword.trim();

        if (trimmedKeyword.isEmpty()) {
            return null;
        }

        return trimmedKeyword;
    }

    private List<HomeFilterOption> mapFilterOptions(
            List<Object[]> rows
    ) {
        return rows.stream()
                .map(row ->
                        new HomeFilterOption(
                                ((Number) row[0]).intValue(),
                                (String) row[1]
                        )
                )
                .toList();
    }

    private Map<Long, List<String>> getTagMap(
            List<Long> activityIds
    ) {

        if (activityIds.isEmpty()) {
            return Map.of();
        }

        List<ActivityTag> activityTags =
                activityTagRepository.findByActivity_IdIn(
                        activityIds
                );

        Map<Long, List<String>> tagMap =
                new HashMap<>();

        for (ActivityTag activityTag : activityTags) {

            Long activityId =
                    activityTag.getActivity().getId();

            String tagName =
                    activityTag.getTag().getName();

            tagMap
                    .computeIfAbsent(
                            activityId,
                            key -> new ArrayList<>()
                    )
                    .add(tagName);
        }

        return tagMap;
    }

    private Map<Long, List<String>> getWeatherTagMap(
            List<Long> activityIds
    ) {

        if (activityIds.isEmpty()) {
            return Map.of();
        }

        List<ActivityWeather> activityWeathers =
                activityWeatherRepository.findByActivity_IdIn(
                        activityIds
                );

        Map<Long, List<String>> weatherTagMap =
                new HashMap<>();

        for (ActivityWeather activityWeather :
                activityWeathers) {

            Long activityId =
                    activityWeather.getActivity().getId();

            String weatherTagName =
                    activityWeather.getWeatherTag().getName();

            weatherTagMap
                    .computeIfAbsent(
                            activityId,
                            key -> new ArrayList<>()
                    )
                    .add(weatherTagName);
        }

        return weatherTagMap;
    }

    private HomeItemResponse toActivityResponse(
            Activity activity,
            List<String> tags,
            List<String> weatherTags
    ) {

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

    private HomeItemResponse toRestaurantResponse(
            Restaurant restaurant
    ) {

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