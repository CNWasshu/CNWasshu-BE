package com.example.cnwasshu.domain.timetable.dto.request;

import com.example.cnwasshu.domain.timetable.entity.TimetableScheduleType;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class TimetableSaveRequestTest {

    private static Validator validator;
    private static jakarta.validation.ValidatorFactory validatorFactory;

    @BeforeAll
    static void setUpValidator() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @AfterAll
    static void closeValidatorFactory() {
        validatorFactory.close();
    }

    @Test
    void acceptsValidRequest() {
        TimetableSaveRequest request = validRequest();

        Set<ConstraintViolation<TimetableSaveRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();
    }

    @Test
    void rejectsBlankNameAndEmptyDays() {
        TimetableSaveRequest request = new TimetableSaveRequest(
                " ",
                LocalDate.of(2026, 8, 20),
                LocalDate.of(2026, 8, 22),
                List.of()
        );

        Set<ConstraintViolation<TimetableSaveRequest>> violations = validator.validate(request);

        assertThat(violations)
                .extracting(violation -> violation.getPropertyPath().toString())
                .contains("timetableName", "days");
    }

    @Test
    void validatesNestedSchedule() {
        TimetableScheduleRequest invalidSchedule = new TimetableScheduleRequest(
                " ",
                null,
                null,
                null,
                null,
                " ",
                null,
                null,
                null,
                0
        );
        TimetableSaveRequest request = new TimetableSaveRequest(
                "충남 여행",
                LocalDate.of(2026, 8, 20),
                LocalDate.of(2026, 8, 20),
                List.of(new TimetableDayRequest(
                        1,
                        LocalDate.of(2026, 8, 20),
                        List.of(invalidSchedule)
                ))
        );

        Set<ConstraintViolation<TimetableSaveRequest>> violations = validator.validate(request);

        assertThat(violations)
                .extracting(violation -> violation.getPropertyPath().toString())
                .contains(
                        "days[0].schedules[0].clientScheduleId",
                        "days[0].schedules[0].scheduleType",
                        "days[0].schedules[0].title",
                        "days[0].schedules[0].startTime",
                        "days[0].schedules[0].endTime",
                        "days[0].schedules[0].sortOrder"
                );
    }

    private TimetableSaveRequest validRequest() {
        TimetableScheduleRequest schedule = new TimetableScheduleRequest(
                "local-1",
                TimetableScheduleType.FREE,
                null,
                null,
                null,
                "점심 식사",
                LocalTime.of(12, 0),
                LocalTime.of(13, 0),
                null,
                1
        );
        TimetableDayRequest day = new TimetableDayRequest(
                1,
                LocalDate.of(2026, 8, 20),
                List.of(schedule)
        );
        return new TimetableSaveRequest(
                "충남 여행",
                LocalDate.of(2026, 8, 20),
                LocalDate.of(2026, 8, 20),
                List.of(day)
        );
    }
}
