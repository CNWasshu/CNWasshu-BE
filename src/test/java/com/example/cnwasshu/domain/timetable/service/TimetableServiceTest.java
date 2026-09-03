package com.example.cnwasshu.domain.timetable.service;

import com.example.cnwasshu.domain.course.entity.Course;
import com.example.cnwasshu.domain.course.entity.CourseItem;
import com.example.cnwasshu.domain.course.repository.CourseRepository;
import com.example.cnwasshu.domain.review.service.CourseSurveyGenerationService;
import com.example.cnwasshu.domain.timetable.dto.request.TimetableDayRequest;
import com.example.cnwasshu.domain.timetable.dto.request.TimetableSaveRequest;
import com.example.cnwasshu.domain.timetable.dto.request.TimetableScheduleRequest;
import com.example.cnwasshu.domain.timetable.entity.ActivityOperatingType;
import com.example.cnwasshu.domain.timetable.entity.TimetableScheduleType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TimetableServiceTest {

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private TimetableValidator timetableValidator;

    @Mock
    private SavedActivityQueryPort savedActivityQueryPort;

    @Mock
    private CourseSurveyGenerationService courseSurveyGenerationService;

    @InjectMocks
    private TimetableService timetableService;

    @Test
    void createsUserTimetableWithInternalCourseCompatibilityValues() {
        LocalDate travelDate = LocalDate.of(2026, 8, 21);
        TimetableSaveRequest request = new TimetableSaveRequest(
                " 충남 당일치기 ",
                travelDate,
                travelDate,
                List.of(new TimetableDayRequest(1, travelDate, List.of()))
        );
        when(courseRepository.saveAndFlush(any(Course.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(timetableValidator.validate(1L, request)).thenReturn(emptyReferenceData());

        timetableService.createTimetable(1L, request);

        ArgumentCaptor<Course> courseCaptor = ArgumentCaptor.forClass(Course.class);
        verify(courseRepository).saveAndFlush(courseCaptor.capture());
        Course savedCourse = courseCaptor.getValue();
        assertThat(savedCourse.getCourseName()).isEqualTo("충남 당일치기");
        assertThat(savedCourse.getPeopleCount()).isEqualTo(1);
        assertThat(savedCourse.getWithChild()).isFalse();
        verify(courseSurveyGenerationService).generateFor(savedCourse);
    }

    @Test
    void savesOriginalLocationsForActivityAndRestaurantSchedules() {
        LocalDate travelDate = LocalDate.of(2026, 8, 21);
        TimetableScheduleRequest activitySchedule = new TimetableScheduleRequest(
                "activity-1", TimetableScheduleType.ACTIVITY, 10L, null, null,
                " 체험 ", LocalTime.of(10, 0), LocalTime.of(11, 0), null, 1
        );
        TimetableScheduleRequest restaurantSchedule = new TimetableScheduleRequest(
                "restaurant-1", TimetableScheduleType.RESTAURANT, null, 20L, null,
                " 식당 ", LocalTime.of(12, 0), LocalTime.of(13, 0), null, 2
        );
        TimetableScheduleRequest freeSchedule = new TimetableScheduleRequest(
                "free-1", TimetableScheduleType.FREE, null, null, null,
                " 자유 일정 ", LocalTime.of(14, 0), LocalTime.of(15, 0), null, 3
        );
        TimetableSaveRequest request = new TimetableSaveRequest(
                "당일 코스", travelDate, travelDate,
                List.of(new TimetableDayRequest(
                        1,
                        travelDate,
                        List.of(activitySchedule, restaurantSchedule, freeSchedule)
                ))
        );
        TimetableReferenceData referenceData = new TimetableReferenceData(
                Map.of(10L, new TimetableActivityInfo(
                        10L, ActivityOperatingType.ALWAYS, null, null, 60, false,
                        "체험 주소", new BigDecimal("36.1234567"), new BigDecimal("126.1234567")
                )),
                Map.of(20L, new TimetableRestaurantInfo(
                        20L, ActivityOperatingType.ALWAYS, null, null,
                        "식당 주소", new BigDecimal("36.7654321"), new BigDecimal("126.7654321")
                )),
                Collections.emptyMap()
        );
        when(timetableValidator.validate(1L, request)).thenReturn(referenceData);
        when(courseRepository.saveAndFlush(any(Course.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        timetableService.createTimetable(1L, request);

        ArgumentCaptor<Course> courseCaptor = ArgumentCaptor.forClass(Course.class);
        verify(courseRepository).saveAndFlush(courseCaptor.capture());
        List<CourseItem> items = courseCaptor.getValue().getItems();
        assertThat(items).extracting(CourseItem::getAddress)
                .containsExactly("체험 주소", "식당 주소", null);
        assertThat(items).extracting(CourseItem::getLatitude)
                .containsExactly(new BigDecimal("36.1234567"), new BigDecimal("36.7654321"), null);
        assertThat(items).extracting(CourseItem::getLongitude)
                .containsExactly(new BigDecimal("126.1234567"), new BigDecimal("126.7654321"), null);
    }

    private TimetableReferenceData emptyReferenceData() {
        return new TimetableReferenceData(
                Collections.emptyMap(),
                Collections.emptyMap(),
                Collections.emptyMap()
        );
    }
}
