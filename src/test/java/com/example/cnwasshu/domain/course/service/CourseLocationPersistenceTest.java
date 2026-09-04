package com.example.cnwasshu.domain.course.service;

import com.example.cnwasshu.domain.course.dto.request.CourseItemRequest;
import com.example.cnwasshu.domain.course.dto.request.CourseSaveRequest;
import com.example.cnwasshu.domain.course.entity.Course;
import com.example.cnwasshu.domain.course.entity.CourseItem;
import com.example.cnwasshu.domain.course.entity.CourseType;
import com.example.cnwasshu.domain.course.repository.CourseRepository;
import com.example.cnwasshu.domain.review.service.CourseSurveyGenerationService;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

class CourseLocationPersistenceTest {

    private final CourseRepository courseRepository = mock(CourseRepository.class);
    private final CourseSurveyGenerationService courseSurveyGenerationService =
            mock(CourseSurveyGenerationService.class);
    private final KakaoMobilityDirectionsClient directionsClient = mock(KakaoMobilityDirectionsClient.class);
    private final CourseService service = new CourseService(
            courseRepository, courseSurveyGenerationService, directionsClient);

    @Test
    void savesAndReturnsAiCourseLocationWithoutChangingValues() {
        when(courseRepository.saveAndFlush(any(Course.class))).thenAnswer(invocation -> invocation.getArgument(0));
        CourseSaveRequest request = request(new BigDecimal("36.4623000"), new BigDecimal("127.1277000"));

        var saved = service.saveAiCourse(1L, request);
        Course savedEntity = captureSavedCourse();
        when(courseRepository.findByIdAndUserIdAndDeletedAtIsNull(10L, 1L))
                .thenReturn(Optional.of(savedEntity));
        var loaded = service.getCourseDetail(1L, 10L);

        assertThat(saved.items().get(0).address()).isEqualTo("충남 공주시 웅진로 280");
        assertThat(loaded.items().get(0).latitude()).isEqualByComparingTo("36.4623000");
        assertThat(loaded.items().get(0).longitude()).isEqualByComparingTo("127.1277000");
        verify(courseSurveyGenerationService).generateFor(savedEntity);
    }

    @Test
    void allowsUserCourseWithoutLocation() {
        when(courseRepository.saveAndFlush(any(Course.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var saved = service.saveManualCourse(1L, request(null, null));

        assertThat(saved.items().get(0).latitude()).isNull();
        assertThat(saved.items().get(0).longitude()).isNull();
    }

    @Test
    void rejectsOnlyOneCoordinate() {
        assertThatThrownBy(() -> service.saveAiCourse(
                1L, request(new BigDecimal("36.4623000"), null)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("둘 다");
    }

    @Test
    void cancelsIncompleteSurveysWhenCourseIsDeleted() {
        Course course = Course.builder()
                .userId(1L)
                .courseName("삭제할 코스")
                .courseType(CourseType.USER)
                .peopleCount(2)
                .withChild(false)
                .startDate(LocalDate.of(2026, 9, 10))
                .endDate(LocalDate.of(2026, 9, 10))
                .build();
        when(courseRepository.findByIdAndUserIdAndDeletedAtIsNull(10L, 1L))
                .thenReturn(Optional.of(course));

        service.deleteCourse(1L, 10L);

        assertThat(course.isDeleted()).isTrue();
        verify(courseSurveyGenerationService).cancelForCourse(10L);
    }

    @Test
    void addsCarRouteOnlyBetweenConsecutiveItemsOnTheSameDay() {
        Course course = Course.builder()
                .userId(1L)
                .courseName("이동 코스")
                .courseType(CourseType.USER)
                .peopleCount(2)
                .withChild(false)
                .startDate(LocalDate.of(2026, 9, 10))
                .endDate(LocalDate.of(2026, 9, 11))
                .build();
        course.addItem(item("첫 장소", 1, 1, "36.1", "127.1"));
        course.addItem(item("둘째 장소", 1, 2, "36.2", "127.2"));
        course.addItem(item("다음 날 장소", 2, 1, "36.3", "127.3"));
        when(courseRepository.findByIdAndUserIdAndDeletedAtIsNull(10L, 1L))
                .thenReturn(Optional.of(course));
        when(directionsClient.getCarRoute(
                new BigDecimal("36.1"), new BigDecimal("127.1"),
                new BigDecimal("36.2"), new BigDecimal("127.2")))
                .thenReturn(Optional.of(new KakaoMobilityDirectionsClient.RouteInfo(12_000, 1_080)));

        var detail = service.getCourseDetail(1L, 10L);

        assertThat(detail.items().get(0).distanceMeters()).isEqualTo(12_000);
        assertThat(detail.items().get(0).travelTimeSeconds()).isEqualTo(1_080);
        assertThat(detail.items().get(1).distanceMeters()).isNull();
        assertThat(detail.items().get(2).distanceMeters()).isNull();
        verify(directionsClient).getCarRoute(
                new BigDecimal("36.1"), new BigDecimal("127.1"),
                new BigDecimal("36.2"), new BigDecimal("127.2"));
        verifyNoMoreInteractions(directionsClient);
    }

    private Course savedCourse;

    private Course captureSavedCourse() {
        return savedCourse;
    }

    private CourseSaveRequest request(BigDecimal latitude, BigDecimal longitude) {
        when(courseRepository.saveAndFlush(any(Course.class))).thenAnswer(invocation -> {
            savedCourse = invocation.getArgument(0);
            return savedCourse;
        });
        CourseItemRequest item = new CourseItemRequest(
                null, null, "국립공주박물관", 1,
                LocalTime.of(10, 0), LocalTime.of(12, 0), 1,
                "충남 공주시 웅진로 280", latitude, longitude, "박물관 관람"
        );
        return new CourseSaveRequest(
                "공주 AI 코스", 2, false,
                LocalDate.of(2026, 9, 10), LocalDate.of(2026, 9, 10), List.of(item)
        );
    }

    private CourseItem item(String title, int dayNo, int sortOrder, String latitude, String longitude) {
        return CourseItem.builder()
                .title(title)
                .dayNo(dayNo)
                .startTime(LocalTime.of(9 + sortOrder, 0))
                .endTime(LocalTime.of(10 + sortOrder, 0))
                .sortOrder(sortOrder)
                .latitude(new BigDecimal(latitude))
                .longitude(new BigDecimal(longitude))
                .build();
    }
}
