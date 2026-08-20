package com.example.cnwasshu.domain.timetable.service;

import com.example.cnwasshu.domain.course.entity.Course;
import com.example.cnwasshu.domain.course.entity.CourseItem;
import com.example.cnwasshu.domain.course.entity.CourseType;
import com.example.cnwasshu.domain.course.repository.CourseRepository;
import com.example.cnwasshu.domain.timetable.dto.request.TimetableSaveRequest;
import com.example.cnwasshu.domain.timetable.dto.request.TimetableScheduleRequest;
import com.example.cnwasshu.domain.timetable.dto.response.TimetableDetailResponse;
import com.example.cnwasshu.domain.timetable.dto.response.TimetableListResponse;
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

    public TimetableListResponse getMyTimetables(Long userId) {
        List<Course> timetables = courseRepository
                .findByUserIdAndDeletedAtIsNullOrderByCreatedAtDesc(userId)
                .stream()
                .filter(course -> course.getCourseType() == CourseType.USER)
                .toList();

        return TimetableListResponse.from(timetables);
    }

    @Transactional
    public TimetableDetailResponse createTimetable(Long userId, TimetableSaveRequest request) {
        timetableValidator.validate(request);

        Course course = Course.builder()
                .userId(userId)
                .courseName(request.timetableName().trim())
                .courseType(CourseType.USER)
                .peopleCount(request.peopleCount())
                .withChild(request.withChild())
                .startDate(request.startDate())
                .endDate(request.endDate())
                .build();

        request.days().forEach(day -> day.schedules().forEach(schedule ->
                course.addItem(toCourseItem(day.dayNo(), schedule))
        ));

        Course savedCourse = courseRepository.save(course);
        return TimetableDetailResponse.from(savedCourse);
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
