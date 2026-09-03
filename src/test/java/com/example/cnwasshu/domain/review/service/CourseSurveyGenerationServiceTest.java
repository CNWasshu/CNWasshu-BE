package com.example.cnwasshu.domain.review.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.example.cnwasshu.domain.course.entity.Course;
import com.example.cnwasshu.domain.course.entity.CourseItem;
import com.example.cnwasshu.domain.course.entity.CourseType;
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

@ExtendWith(MockitoExtension.class)
class CourseSurveyGenerationServiceTest {

    private static final Long USER_ID = 1L;
    private static final Long COURSE_ID = 10L;
    private static final LocalDate COURSE_DATE = LocalDate.of(2026, 9, 3);
    private static final LocalDateTime SCHEDULED_AT = LocalDateTime.of(2026, 9, 3, 19, 0);

    @Mock
    private CourseSurveyRepository courseSurveyRepository;

    @Mock
    private ActivitySurveyRepository activitySurveyRepository;

    @Mock
    private StampRepository stampRepository;

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private SurveyScheduleCalculator scheduleCalculator;

    private CourseSurveyGenerationService generationService;

    @BeforeEach
    void setUp() {
        generationService = new CourseSurveyGenerationService(
                courseSurveyRepository,
                activitySurveyRepository,
                stampRepository,
                reservationRepository,
                scheduleCalculator
        );
    }

    @Test
    void 직접_코스는_체험이_있는_날짜에_코스와_체험_설문을_생성한다() {
        Course course = course(CourseType.USER);
        CourseItem activity = item(100L, null, 1, "공예 체험", 14, 16);
        CourseItem restaurant = item(null, 200L, 1, "저녁 식사", 17, 18);
        course.addItem(activity);
        course.addItem(restaurant);
        assignIds(course, activity, 101L);
        stubNewCourseSurvey();
        when(scheduleCalculator.calculate(COURSE_DATE, LocalTime.of(17, 0), LocalTime.of(18, 0)))
                .thenReturn(SCHEDULED_AT);
        when(stampRepository.findByUserIdAndActivityId(USER_ID, 100L)).thenReturn(Optional.empty());

        generationService.generateFor(course);

        ArgumentCaptor<CourseSurvey> courseSurveyCaptor = ArgumentCaptor.forClass(CourseSurvey.class);
        verify(courseSurveyRepository).save(courseSurveyCaptor.capture());
        assertThat(courseSurveyCaptor.getValue().getSurveyType()).isEqualTo(SurveyType.USER_COURSE);
        assertThat(courseSurveyCaptor.getValue().getScheduledAt()).isEqualTo(SCHEDULED_AT);

        ArgumentCaptor<ActivitySurvey> activitySurveyCaptor = ArgumentCaptor.forClass(ActivitySurvey.class);
        verify(activitySurveyRepository).save(activitySurveyCaptor.capture());
        assertThat(activitySurveyCaptor.getValue().getCourseItemId()).isEqualTo(101L);
        assertThat(activitySurveyCaptor.getValue().getVisitStatus()).isEqualTo(VisitStatus.PENDING);
        assertThat(activitySurveyCaptor.getValue().getVisitEvidenceType()).isEqualTo(VisitEvidenceType.NONE);
    }

    @Test
    void 자유_일정과_음식점만_있는_직접_코스는_설문을_생성하지_않는다() {
        Course course = course(CourseType.USER);
        CourseItem freeSchedule = item(null, null, 1, "숙소 이동", 17, 18);
        CourseItem restaurant = item(null, 200L, 1, "저녁 식사", 18, 19);
        course.addItem(freeSchedule);
        course.addItem(restaurant);
        assignIds(course, freeSchedule, 101L);
        ReflectionTestUtils.setField(restaurant, "id", 102L);

        generationService.generateFor(course);

        verify(courseSurveyRepository, never()).save(any());
        verify(activitySurveyRepository, never()).save(any());
    }

    @Test
    void AI_코스는_코스_설문만_생성한다() {
        Course course = course(CourseType.AI);
        CourseItem activity = item(100L, null, 1, "추천 체험", 14, 16);
        course.addItem(activity);
        assignIds(course, activity, 101L);
        stubNewCourseSurvey();
        when(scheduleCalculator.calculate(COURSE_DATE, LocalTime.of(14, 0), LocalTime.of(16, 0)))
                .thenReturn(SCHEDULED_AT);

        generationService.generateFor(course);

        ArgumentCaptor<CourseSurvey> captor = ArgumentCaptor.forClass(CourseSurvey.class);
        verify(courseSurveyRepository).save(captor.capture());
        assertThat(captor.getValue().getSurveyType()).isEqualTo(SurveyType.AI_COURSE);
        verify(activitySurveyRepository, never()).save(any());
        verify(stampRepository, never()).findByUserIdAndActivityId(any(), any());
    }

    @Test
    void 스탬프가_있으면_방문한_체험으로_생성한다() {
        Course course = course(CourseType.USER);
        CourseItem activity = item(100L, null, 1, "공예 체험", 14, 16);
        course.addItem(activity);
        assignIds(course, activity, 101L);
        stubNewCourseSurvey();
        when(scheduleCalculator.calculate(any(), any(), any())).thenReturn(SCHEDULED_AT);
        Stamp stamp = mock(Stamp.class);
        when(stamp.getId()).thenReturn(300L);
        when(stampRepository.findByUserIdAndActivityId(USER_ID, 100L)).thenReturn(Optional.of(stamp));

        generationService.generateFor(course);

        ArgumentCaptor<ActivitySurvey> captor = ArgumentCaptor.forClass(ActivitySurvey.class);
        verify(activitySurveyRepository).save(captor.capture());
        assertThat(captor.getValue().getStampId()).isEqualTo(300L);
        assertThat(captor.getValue().getVisitStatus()).isEqualTo(VisitStatus.VISITED);
        assertThat(captor.getValue().getVisitEvidenceType()).isEqualTo(VisitEvidenceType.STAMP);
    }

    private void stubNewCourseSurvey() {
        when(courseSurveyRepository.findByUserIdAndCourseIdAndCourseDateAndSurveyType(
                any(), any(), any(), any()
        )).thenReturn(Optional.empty());
        when(courseSurveyRepository.save(any(CourseSurvey.class))).thenAnswer(invocation -> {
            CourseSurvey survey = invocation.getArgument(0);
            ReflectionTestUtils.setField(survey, "id", 500L);
            return survey;
        });
    }

    private Course course(CourseType courseType) {
        return Course.builder()
                .userId(USER_ID)
                .courseName("충남 여행")
                .courseType(courseType)
                .peopleCount(2)
                .withChild(false)
                .startDate(COURSE_DATE)
                .endDate(COURSE_DATE)
                .build();
    }

    private CourseItem item(
            Long activityId,
            Long restaurantId,
            int dayNo,
            String title,
            int startHour,
            int endHour
    ) {
        return CourseItem.builder()
                .activityId(activityId)
                .restaurantId(restaurantId)
                .title(title)
                .dayNo(dayNo)
                .startTime(LocalTime.of(startHour, 0))
                .endTime(LocalTime.of(endHour, 0))
                .sortOrder(1)
                .build();
    }

    private void assignIds(Course course, CourseItem item, Long itemId) {
        ReflectionTestUtils.setField(course, "id", COURSE_ID);
        ReflectionTestUtils.setField(item, "id", itemId);
    }
}
