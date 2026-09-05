package com.example.cnwasshu.domain.timetable.service;

import com.example.cnwasshu.domain.course.entity.Course;
import com.example.cnwasshu.domain.course.entity.CourseItem;
import com.example.cnwasshu.domain.course.entity.CourseType;
import com.example.cnwasshu.domain.course.exception.CourseNotFoundException;
import com.example.cnwasshu.domain.course.repository.CourseRepository;
import com.example.cnwasshu.domain.review.service.CourseSurveyGenerationService;
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
    private final CourseSurveyGenerationService courseSurveyGenerationService;

    public SavedActivityListResponse getSavedActivities(Long userId) {
        return SavedActivityListResponse.from(savedActivityQueryPort.findSavedActivities(userId));
    }

    @Transactional
    public TimetableDetailResponse createTimetable(Long userId, TimetableSaveRequest request) {
        TimetableReferenceData referenceData = timetableValidator.validate(userId, request);

        Course course = Course.builder()
                .userId(userId)
                .courseName(request.timetableName().trim())
                .courseType(CourseType.USER)
                .peopleCount(DEFAULT_PEOPLE_COUNT)
                .withChild(DEFAULT_WITH_CHILD)
                .startDate(request.startDate())
                .endDate(request.endDate())
                .build();

        toCourseItems(request, referenceData).forEach(course::addItem);

        Course savedCourse = courseRepository.saveAndFlush(course);
        courseSurveyGenerationService.generateFor(savedCourse);
        return TimetableDetailResponse.from(savedCourse);
    }

    /** 기존에 저장된 코스의 이름/기간/일정을 통째로 교체한다 ("코스 내용 수정"). */
    @Transactional
    public TimetableDetailResponse updateTimetable(Long userId, Long courseId, TimetableSaveRequest request) {
        Course course = courseRepository.findByIdAndUserIdAndDeletedAtIsNull(courseId, userId)
                .orElseThrow(() -> new CourseNotFoundException(courseId));

        TimetableReferenceData referenceData = timetableValidator.validate(userId, request);

        course.updateTimetable(
                request.timetableName().trim(),
                DEFAULT_PEOPLE_COUNT,
                DEFAULT_WITH_CHILD,
                request.startDate(),
                request.endDate()
        );
        course.replaceItems(toCourseItems(request, referenceData));

        Course savedCourse = courseRepository.saveAndFlush(course);
        // 기간/일정이 바뀌었을 수 있으니 예정된 만족도 조사도 다시 계산한다.
        courseSurveyGenerationService.generateFor(savedCourse);
        return TimetableDetailResponse.from(savedCourse);
    }

    private List<CourseItem> toCourseItems(TimetableSaveRequest request, TimetableReferenceData referenceData) {
        return request.days().stream()
                .flatMap(day -> day.schedules().stream()
                        .map(schedule -> toCourseItem(day.dayNo(), schedule, referenceData)))
                .toList();
    }

    private CourseItem toCourseItem(
            Integer dayNo,
            TimetableScheduleRequest schedule,
            TimetableReferenceData referenceData
    ) {
        TimetableActivityInfo activity = schedule.activityId() == null
                ? null
                : referenceData.activities().get(schedule.activityId());
        TimetableRestaurantInfo restaurant = schedule.restaurantId() == null
                ? null
                : referenceData.restaurants().get(schedule.restaurantId());

        return CourseItem.builder()
                .activityId(schedule.activityId())
                .restaurantId(schedule.restaurantId())
                .reservationId(schedule.reservationId())
                .title(schedule.title().trim())
                .dayNo(dayNo)
                .startTime(schedule.startTime())
                .endTime(schedule.endTime())
                .memo(normalizeNullableText(schedule.memo()))
                .sortOrder(schedule.sortOrder())
                .address(activity != null ? activity.address() : restaurant != null ? restaurant.address() : null)
                .latitude(activity != null ? activity.latitude() : restaurant != null ? restaurant.latitude() : null)
                .longitude(activity != null ? activity.longitude() : restaurant != null ? restaurant.longitude() : null)
                .build();
    }

    private String normalizeNullableText(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
