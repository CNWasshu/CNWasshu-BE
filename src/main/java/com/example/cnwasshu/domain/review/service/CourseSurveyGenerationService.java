package com.example.cnwasshu.domain.review.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.cnwasshu.domain.course.entity.Course;
import com.example.cnwasshu.domain.course.entity.CourseItem;
import com.example.cnwasshu.domain.course.entity.CourseType;
import com.example.cnwasshu.domain.reservation.entity.ReservationStatus;
import com.example.cnwasshu.domain.reservation.repository.ReservationRepository;
import com.example.cnwasshu.domain.review.entity.ActivitySurvey;
import com.example.cnwasshu.domain.review.entity.CourseSurvey;
import com.example.cnwasshu.domain.review.entity.SurveyType;
import com.example.cnwasshu.domain.review.entity.VisitEvidenceType;
import com.example.cnwasshu.domain.review.entity.VisitStatus;
import com.example.cnwasshu.domain.review.repository.ActivitySurveyRepository;
import com.example.cnwasshu.domain.review.repository.CourseSurveyRepository;
import com.example.cnwasshu.domain.stamp.entity.Stamp;
import com.example.cnwasshu.domain.stamp.repository.StampRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class CourseSurveyGenerationService {

    private final CourseSurveyRepository courseSurveyRepository;
    private final ActivitySurveyRepository activitySurveyRepository;
    private final StampRepository stampRepository;
    private final ReservationRepository reservationRepository;
    private final SurveyScheduleCalculator scheduleCalculator;

    public void generateFor(Course course) {
        if (course.getId() == null) {
            throw new IllegalArgumentException("저장된 코스만 만족도 조사를 생성할 수 있습니다.");
        }
        if (course.isDeleted()) {
            return;
        }

        Map<Integer, List<CourseItem>> itemsByDay = course.getItems().stream()
                .collect(Collectors.groupingBy(CourseItem::getDayNo));

        itemsByDay.forEach((dayNo, dayItems) -> generateForDay(course, dayNo, dayItems));
    }

    public void cancelForCourse(Long courseId) {
        courseSurveyRepository.findAllByCourseId(courseId)
                .forEach(CourseSurvey::cancelIfIncomplete);
    }

    private void generateForDay(Course course, Integer dayNo, List<CourseItem> dayItems) {
        List<CourseItem> activityItems = dayItems.stream()
                .filter(item -> item.getActivityId() != null)
                .toList();

        if (course.getCourseType() == CourseType.USER && activityItems.isEmpty()) {
            return;
        }

        LocalDate courseDate = course.getStartDate().plusDays(dayNo - 1L);
        CourseItem lastItem = findLastItem(courseDate, dayItems);
        LocalDateTime scheduledAt = scheduleCalculator.calculate(
                courseDate,
                lastItem.getStartTime(),
                lastItem.getEndTime()
        );
        SurveyType surveyType = course.getCourseType() == CourseType.AI
                ? SurveyType.AI_COURSE
                : SurveyType.USER_COURSE;

        CourseSurvey courseSurvey = courseSurveyRepository
                .findByUserIdAndCourseIdAndCourseDateAndSurveyType(
                        course.getUserId(),
                        course.getId(),
                        courseDate,
                        surveyType
                )
                .map(existing -> {
                    existing.reschedule(scheduledAt);
                    return existing;
                })
                .orElseGet(() -> courseSurveyRepository.save(CourseSurvey.schedule(
                        course.getUserId(),
                        course.getId(),
                        courseDate,
                        surveyType,
                        scheduledAt
                )));

        if (surveyType == SurveyType.USER_COURSE) {
            activityItems.forEach(item -> createActivitySurveyIfAbsent(course, courseSurvey, item));
        }
    }

    private CourseItem findLastItem(LocalDate courseDate, List<CourseItem> dayItems) {
        return dayItems.stream()
                .max(Comparator.comparing(item -> endDateTime(courseDate, item)))
                .orElseThrow(() -> new IllegalArgumentException("만족도 조사 대상 일정이 필요합니다."));
    }

    private LocalDateTime endDateTime(LocalDate courseDate, CourseItem item) {
        LocalDateTime endDateTime = courseDate.atTime(item.getEndTime());
        if (item.getEndTime().isBefore(item.getStartTime())) {
            return endDateTime.plusDays(1);
        }
        return endDateTime;
    }

    private void createActivitySurveyIfAbsent(
            Course course,
            CourseSurvey courseSurvey,
            CourseItem item
    ) {
        if (activitySurveyRepository.existsByCourseSurveyIdAndCourseItemId(
                courseSurvey.getId(),
                item.getId()
        )) {
            return;
        }

        VisitEvidence evidence = resolveVisitEvidence(course.getUserId(), item);
        activitySurveyRepository.save(ActivitySurvey.create(
                courseSurvey,
                course.getUserId(),
                item.getId(),
                item.getActivityId(),
                evidence.stampId(),
                evidence.reservationId(),
                evidence.visitStatus(),
                evidence.evidenceType()
        ));
    }

    private VisitEvidence resolveVisitEvidence(Long userId, CourseItem item) {
        Stamp stamp = stampRepository.findByUserIdAndActivityId(userId, item.getActivityId())
                .orElse(null);
        if (stamp != null) {
            return new VisitEvidence(
                    stamp.getId(),
                    item.getReservationId(),
                    VisitStatus.VISITED,
                    VisitEvidenceType.STAMP
            );
        }

        boolean confirmedReservation = item.getReservationId() != null
                && reservationRepository.findByIdAndUserIdAndDeletedAtIsNull(item.getReservationId(), userId)
                .filter(reservation -> reservation.getStatus() == ReservationStatus.CONFIRMED)
                .isPresent();
        if (confirmedReservation) {
            return new VisitEvidence(
                    null,
                    item.getReservationId(),
                    VisitStatus.VISITED,
                    VisitEvidenceType.RESERVATION
            );
        }

        return new VisitEvidence(
                null,
                item.getReservationId(),
                VisitStatus.PENDING,
                VisitEvidenceType.NONE
        );
    }

    private record VisitEvidence(
            Long stampId,
            Long reservationId,
            VisitStatus visitStatus,
            VisitEvidenceType evidenceType
    ) {
    }
}
