package com.example.cnwasshu.domain.timetable.service;

import com.example.cnwasshu.domain.course.entity.Course;
import com.example.cnwasshu.domain.course.entity.CourseItem;
import com.example.cnwasshu.domain.course.entity.CourseType;
import com.example.cnwasshu.domain.course.repository.CourseRepository;
import com.example.cnwasshu.domain.timetable.dto.request.TimetableSaveRequest;
import com.example.cnwasshu.domain.timetable.dto.request.TimetableScheduleRequest;
import com.example.cnwasshu.domain.timetable.dto.response.TimetableDetailResponse;
import com.example.cnwasshu.domain.timetable.dto.response.TimetableListResponse;
import com.example.cnwasshu.domain.timetable.dto.response.SavedActivityListResponse;
import com.example.cnwasshu.domain.timetable.exception.TimetableNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TimetableService {

    private final CourseRepository courseRepository;
    private final TimetableValidator timetableValidator;
    private final SavedActivityQueryPort savedActivityQueryPort;

    public SavedActivityListResponse getSavedActivities(Long userId) {
        return SavedActivityListResponse.from(savedActivityQueryPort.findSavedActivities(userId));
    }

    public TimetableListResponse getMyTimetables(Long userId) {
        List<Course> timetables = courseRepository
                .findByUserIdAndDeletedAtIsNullOrderByCreatedAtDesc(userId)
                .stream()
                .filter(course -> course.getCourseType() == CourseType.USER)
                .toList();

        return TimetableListResponse.from(timetables);
    }

    public TimetableDetailResponse getTimetable(Long userId, Long timetableId) {
        return TimetableDetailResponse.from(findOwnedTimetable(userId, timetableId));
    }

    @Transactional
    public TimetableDetailResponse createTimetable(Long userId, TimetableSaveRequest request) {
        timetableValidator.validate(userId, request);

        Course course = Course.builder()
                .userId(userId)
                .courseName(request.timetableName().trim())
                .courseType(CourseType.USER)
                .peopleCount(request.peopleCount())
                .withChild(request.withChild())
                .startDate(request.startDate())
                .endDate(request.endDate())
                .build();

        toCourseItems(request).forEach(course::addItem);

        Course savedCourse = courseRepository.saveAndFlush(course);
        return TimetableDetailResponse.from(savedCourse);
    }

    @Transactional
    public TimetableDetailResponse updateTimetable(
            Long userId,
            Long timetableId,
            TimetableSaveRequest request
    ) {
        timetableValidator.validate(userId, request);
        Course course = findOwnedTimetable(userId, timetableId);

        course.updateTimetable(
                request.timetableName().trim(),
                request.peopleCount(),
                request.withChild(),
                request.startDate(),
                request.endDate()
        );
        course.replaceItems(toCourseItems(request));

        Course updatedCourse = courseRepository.saveAndFlush(course);
        return TimetableDetailResponse.from(updatedCourse);
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

    private Course findOwnedTimetable(Long userId, Long timetableId) {
        return courseRepository.findByIdAndUserIdAndDeletedAtIsNull(timetableId, userId)
                .filter(course -> course.getCourseType() == CourseType.USER)
                .orElseThrow(() -> new TimetableNotFoundException(timetableId));
    }

    private String normalizeNullableText(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
