package com.example.cnwasshu.domain.course.service;

import com.example.cnwasshu.domain.course.dto.request.CourseItemRequest;
import com.example.cnwasshu.domain.course.dto.request.CourseSaveRequest;
import com.example.cnwasshu.domain.course.dto.response.CourseDetailResponse;
import com.example.cnwasshu.domain.course.dto.response.CourseSummaryResponse;
import com.example.cnwasshu.domain.course.entity.Course;
import com.example.cnwasshu.domain.course.entity.CourseItem;
import com.example.cnwasshu.domain.course.entity.CourseType;
import com.example.cnwasshu.domain.course.exception.CourseNotFoundException;
import com.example.cnwasshu.domain.course.exception.CourseTimeOverlapException;
import com.example.cnwasshu.domain.course.repository.CourseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CourseService {

    private final CourseRepository courseRepository;

    public List<CourseSummaryResponse> getMyCourses(Long userId) {
        return courseRepository.findByUserIdAndDeletedAtIsNullOrderByCreatedAtDesc(userId)
                .stream()
                .map(CourseSummaryResponse::from)
                .toList();
    }

    public CourseDetailResponse getCourseDetail(Long userId, Long courseId) {
        Course course = findOwnedCourse(userId, courseId);
        return CourseDetailResponse.from(course);
    }

    @Transactional
    public CourseDetailResponse saveManualCourse(Long userId, CourseSaveRequest request) {
        return saveCourse(userId, request, CourseType.USER);
    }

    @Transactional
    public CourseDetailResponse saveAiCourse(Long userId, CourseSaveRequest request) {
        return saveCourse(userId, request, CourseType.AI);
    }

    private CourseDetailResponse saveCourse(Long userId, CourseSaveRequest request, CourseType type) {
        validateNoOverlap(request.items());

        Course course = Course.builder()
                .userId(userId)
                .courseName(request.courseName())
                .courseType(type)
                .peopleCount(request.peopleCount())
                .withChild(request.withChild())
                .startDate(request.startDate())
                .endDate(request.endDate())
                .build();

        request.items().forEach(itemRequest -> course.addItem(toEntity(itemRequest)));

        Course saved = courseRepository.save(course);
        return CourseDetailResponse.from(saved);
    }

    @Transactional
    public void renameCourse(Long userId, Long courseId, String newName) {
        Course course = findOwnedCourse(userId, courseId);
        course.rename(newName);
    }

    @Transactional
    public void deleteCourse(Long userId, Long courseId) {
        Course course = findOwnedCourse(userId, courseId);
        course.softDelete();
    }

    private Course findOwnedCourse(Long userId, Long courseId) {
        return courseRepository.findByIdAndUserIdAndDeletedAtIsNull(courseId, userId)
                .orElseThrow(() -> new CourseNotFoundException(courseId));
    }

    /** 목업의 hasOverlap 로직을 서버에서도 동일하게 검증 (day_no 단위로 시간 겹침 체크) */
    private void validateNoOverlap(List<CourseItemRequest> items) {
        for (int i = 0; i < items.size(); i++) {
            for (int j = i + 1; j < items.size(); j++) {
                CourseItemRequest a = items.get(i);
                CourseItemRequest b = items.get(j);
                if (!a.dayNo().equals(b.dayNo())) continue;
                boolean overlap = a.startTime().isBefore(b.endTime()) && b.startTime().isBefore(a.endTime());
                if (overlap) {
                    throw new CourseTimeOverlapException(a.dayNo());
                }
            }
        }
    }

    private CourseItem toEntity(CourseItemRequest request) {
        return CourseItem.builder()
                .activityId(request.activityId())
                .reservationId(request.reservationId())
                .title(request.title())
                .dayNo(request.dayNo())
                .startTime(request.startTime())
                .endTime(request.endTime())
                .memo(request.memo())
                .sortOrder(request.sortOrder())
                .build();
    }
}