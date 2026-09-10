package com.example.cnwasshu.domain.timetable.repository;

import com.example.cnwasshu.domain.timetable.entity.ActivityOperatingType;
import com.example.cnwasshu.domain.timetable.service.SavedActivityInfo;
import com.example.cnwasshu.domain.timetable.service.SavedActivityQueryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Time;
import java.time.LocalTime;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class JdbcSavedActivityQueryAdapter implements SavedActivityQueryPort {

    private static final String FIND_SAVED_ACTIVITIES = """
            SELECT a.activity_id,
                   a.title,
                   a.thumbnail,
                   r.region_name,
                   a.operating_start_time,
                   a.operating_end_time,
                   a.duration,
                   a.reservation_required
              FROM user_bookmark ub
              JOIN activity a ON a.activity_id = ub.activity_id
              JOIN region r ON r.region_id = a.region_id
             WHERE ub.user_id = ?
               AND a.deleted_at IS NULL
             ORDER BY ub.created_at DESC
            """;

    private final JdbcTemplate jdbcTemplate;

    @Override
    public List<SavedActivityInfo> findSavedActivities(Long userId) {
        return jdbcTemplate.query(
                FIND_SAVED_ACTIVITIES,
                (resultSet, rowNumber) -> {
                    LocalTime operatingStartTime = toLocalTime(resultSet.getTime("operating_start_time"));
                    LocalTime operatingEndTime = toLocalTime(resultSet.getTime("operating_end_time"));

                    return new SavedActivityInfo(
                            resultSet.getLong("activity_id"),
                            resultSet.getString("title"),
                            resultSet.getString("thumbnail"),
                            resultSet.getString("region_name"),
                            resolveOperatingType(operatingStartTime, operatingEndTime),
                            operatingStartTime,
                            operatingEndTime,
                            resultSet.getObject("duration", Integer.class),
                            resultSet.getBoolean("reservation_required")
                    );
                },
                userId
        );
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
