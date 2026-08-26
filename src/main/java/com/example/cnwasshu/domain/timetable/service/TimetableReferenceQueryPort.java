package com.example.cnwasshu.domain.timetable.service;

import java.util.Map;
import java.util.Set;

public interface TimetableReferenceQueryPort {

    Map<Long, TimetableActivityInfo> findActivities(Set<Long> activityIds);

    Map<Long, TimetableRestaurantInfo> findRestaurants(Set<Long> restaurantIds);

    Map<Long, TimetableReservationInfo> findReservations(Set<Long> reservationIds);
}
