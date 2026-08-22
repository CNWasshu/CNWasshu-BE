package com.example.cnwasshu.domain.timetable.service;

import com.example.cnwasshu.domain.course.entity.Course;
import com.example.cnwasshu.domain.course.entity.CourseItem;
import com.example.cnwasshu.domain.course.entity.CourseType;
import com.example.cnwasshu.domain.course.repository.CourseRepository;
import com.example.cnwasshu.domain.timetable.dto.request.TimetableSaveRequest;
import com.example.cnwasshu.domain.timetable.dto.request.TimetableScheduleRequest;
import com.example.cnwasshu.domain.timetable.dto.response.TimetableDetailResponse;
import com.example.cnwasshu.domain.timetable.dto.response.SavedActivityListResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TimetableService {

    // Course의 공통 NOT NULL 제약을 유지하기 위한 USER 타임테이블 내부 호환값이다.
    // 타임테이블 API의 여행 조건으로 사용하거나 외부 계약에 노출하지 않는다.
    private static final int DEFAULT_PEOPLE_COUNT = 1;
    private static final boolean DEFAULT_WITH_CHILD = false;

    private final CourseRepository courseRepository;
    private final TimetableValidator timetableValidator;
    private final SavedActivityQueryPort savedActivityQueryPort;

    public SavedActivityListResponse getSavedActivities(Long userId) {
        return SavedActivityListResponse.from(savedActivityQueryPort.findSavedActivities(userId));
    }

    @Transactional
    public TimetableDetailResponse createTimetable(Long userId, TimetableSaveRequest request) {
        timetableValidator.validate(userId, request);

        Course course = Course.builder()
                .userId(userId)
                .courseName(request.timetableName().trim())
                .courseType(CourseType.USER)
                .peopleCount(DEFAULT_PEOPLE_COUNT)
                .withChild(DEFAULT_WITH_CHILD)
                .startDate(request.startDate())
                .endDate(request.endDate())
                .build();

        toCourseItems(request).forEach(course::addItem);

        Course savedCourse = courseRepository.saveAndFlush(course);
        return TimetableDetailResponse.from(savedCourse);
    }

    private List<CourseItem> toCourseItems(TimetableSaveRequest request) {
        return request.days().stream()
                .flatMap(day -> day.schedules().stream()
                        .map(schedule -> toCourseItem(day.dayNo(), schedule)))
                .toList();
    }

    private CourseItem toCourseItem(Integer dayNo, TimetableScheduleRequest schedule) {
        return CourseItem.builder()
                .activityId(schedule.activityId())
                .reservationId(schedule.reservationId())
                .title(schedule.title().trim())
                .dayNo(dayNo)
                .startTime(schedule.startTime())
                .endTime(schedule.endTime())
                .memo(normalizeNullableText(schedule.memo()))
                .sortOrder(schedule.sortOrder())
                .build();
    }

    private String normalizeNullableText(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
