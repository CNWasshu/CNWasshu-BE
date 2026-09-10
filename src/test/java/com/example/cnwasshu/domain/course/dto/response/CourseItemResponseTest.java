package com.example.cnwasshu.domain.course.dto.response;

import org.junit.jupiter.api.Test;

import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CourseItemResponseTest {

    @Test
    void sortsByDayThenSortOrderThenStartTime() {
        CourseItemResponse dayTwo = item(2, 1, LocalTime.of(9, 0), "day-2");
        CourseItemResponse second = item(1, 2, LocalTime.of(11, 0), "second");
        CourseItemResponse first = item(1, 1, LocalTime.of(10, 0), "first");

        List<CourseItemResponse> sorted = List.of(dayTwo, second, first).stream()
                .sorted(CourseItemResponse.COURSE_ORDER)
                .toList();

        assertThat(sorted).extracting(CourseItemResponse::title)
                .containsExactly("first", "second", "day-2");
    }

    private CourseItemResponse item(int dayNo, int sortOrder, LocalTime startTime, String title) {
        return new CourseItemResponse(
                null, null, null, null, title, dayNo, startTime, startTime.plusHours(1), sortOrder,
                null, null, null, null, null, null
        );
    }
}
