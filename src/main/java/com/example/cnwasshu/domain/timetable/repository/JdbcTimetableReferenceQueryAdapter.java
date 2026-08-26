package com.example.cnwasshu.domain.timetable.repository;

import com.example.cnwasshu.domain.timetable.entity.ActivityOperatingType;
import com.example.cnwasshu.domain.timetable.service.TimetableActivityInfo;
import com.example.cnwasshu.domain.timetable.service.TimetableReferenceQueryPort;
import com.example.cnwasshu.domain.timetable.service.TimetableReservationInfo;
import com.example.cnwasshu.domain.timetable.service.TimetableRestaurantInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Time;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class JdbcTimetableReferenceQueryAdapter implements TimetableReferenceQueryPort {

    private static final String FIND_ACTIVITIES = """
            SELECT activity_id,
                   operating_start_time,
                   operating_end_time,
                   duration,
                   reservation_required
              FROM activity
             WHERE activity_id IN (:activityIds)
               AND deleted_at IS NULL
            """;

    private static final String FIND_RESERVATIONS = """
            SELECT reservation_id,
                   user_id,
                   activity_id,
                   reservation_date,
                   reservation_time,
                   status
              FROM reservation
             WHERE reservation_id IN (:reservationIds)
               AND deleted_at IS NULL
            """;

    private static final String FIND_RESTAURANTS = """
            SELECT restaurant_id,
                   operating_start_time,
                   operating_end_time
              FROM restaurant
             WHERE restaurant_id IN (:restaurantIds)
               AND deleted_at IS NULL
            """;

    private final NamedParameterJdbcTemplate jdbcTemplate;

    @Override
    public Map<Long, TimetableActivityInfo> findActivities(Set<Long> activityIds) {
        if (activityIds.isEmpty()) {
            return Collections.emptyMap();
        }

        List<TimetableActivityInfo> activities = jdbcTemplate.query(
                FIND_ACTIVITIES,
                new MapSqlParameterSource("activityIds", activityIds),
                (resultSet, rowNumber) -> {
                    LocalTime operatingStartTime = toLocalTime(resultSet.getTime("operating_start_time"));
                    LocalTime operatingEndTime = toLocalTime(resultSet.getTime("operating_end_time"));

                    return new TimetableActivityInfo(
                            resultSet.getLong("activity_id"),
                            resolveOperatingType(operatingStartTime, operatingEndTime),
                            operatingStartTime,
                            operatingEndTime,
                            resultSet.getObject("duration", Integer.class),
                            resultSet.getBoolean("reservation_required")
                    );
                }
        );

        return activities.stream().collect(Collectors.toUnmodifiableMap(
                TimetableActivityInfo::activityId,
                Function.identity()
        ));
    }

    @Override
    public Map<Long, TimetableReservationInfo> findReservations(Set<Long> reservationIds) {
        if (reservationIds.isEmpty()) {
            return Collections.emptyMap();
        }

        List<TimetableReservationInfo> reservations = jdbcTemplate.query(
                FIND_RESERVATIONS,
                new MapSqlParameterSource("reservationIds", reservationIds),
                (resultSet, rowNumber) -> new TimetableReservationInfo(
                        resultSet.getLong("reservation_id"),
                        resultSet.getLong("user_id"),
                        resultSet.getLong("activity_id"),
                        resultSet.getDate("reservation_date").toLocalDate(),
                        resultSet.getTime("reservation_time").toLocalTime(),
                        resultSet.getString("status")
                )
        );

        return reservations.stream().collect(Collectors.toUnmodifiableMap(
                TimetableReservationInfo::reservationId,
                Function.identity()
        ));
    }

    @Override
    public Map<Long, TimetableRestaurantInfo> findRestaurants(Set<Long> restaurantIds) {
        if (restaurantIds.isEmpty()) {
            return Collections.emptyMap();
        }

        List<TimetableRestaurantInfo> restaurants = jdbcTemplate.query(
                FIND_RESTAURANTS,
                new MapSqlParameterSource("restaurantIds", restaurantIds),
                (resultSet, rowNumber) -> {
                    LocalTime operatingStartTime = toLocalTime(resultSet.getTime("operating_start_time"));
                    LocalTime operatingEndTime = toLocalTime(resultSet.getTime("operating_end_time"));

                    return new TimetableRestaurantInfo(
                            resultSet.getLong("restaurant_id"),
                            resolveOperatingType(operatingStartTime, operatingEndTime),
                            operatingStartTime,
                            operatingEndTime
                    );
                }
        );

        return restaurants.stream().collect(Collectors.toUnmodifiableMap(
                TimetableRestaurantInfo::restaurantId,
                Function.identity()
        ));
    }

    private ActivityOperatingType resolveOperatingType(
            LocalTime operatingStartTime,
            LocalTime operatingEndTime
    ) {
        if (operatingStartTime == null && operatingEndTime == null) {
            return ActivityOperatingType.ALWAYS;
        }
        if (operatingStartTime != null && operatingEndTime != null) {
            return ActivityOperatingType.HOURS;
        }
        throw new IllegalStateException("체험 운영 시작 시간과 종료 시간은 둘 다 있거나 둘 다 없어야 합니다.");
    }

    private LocalTime toLocalTime(Time time) {
        return time == null ? null : time.toLocalTime();
    }
}
