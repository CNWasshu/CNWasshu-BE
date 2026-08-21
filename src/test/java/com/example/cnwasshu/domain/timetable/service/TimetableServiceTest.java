package com.example.cnwasshu.domain.timetable.service;

import com.example.cnwasshu.domain.course.entity.Course;
import com.example.cnwasshu.domain.course.repository.CourseRepository;
import com.example.cnwasshu.domain.timetable.dto.request.TimetableDayRequest;
import com.example.cnwasshu.domain.timetable.dto.request.TimetableSaveRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

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

        timetableService.createTimetable(1L, request);

        ArgumentCaptor<Course> courseCaptor = ArgumentCaptor.forClass(Course.class);
        verify(courseRepository).saveAndFlush(courseCaptor.capture());
        Course savedCourse = courseCaptor.getValue();
        assertThat(savedCourse.getCourseName()).isEqualTo("충남 당일치기");
        assertThat(savedCourse.getPeopleCount()).isEqualTo(1);
        assertThat(savedCourse.getWithChild()).isFalse();
    }
}
