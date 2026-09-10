package com.example.cnwasshu.domain.review.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SurveyScheduleCalculatorTest {

    private static final LocalDate COURSE_DATE = LocalDate.of(2026, 9, 3);

    private SurveyScheduleCalculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new SurveyScheduleCalculator();
    }

    @Test
    void 마지막_일정_종료_한_시간_후로_계산한다() {
        LocalDateTime scheduledAt = calculator.calculate(
                COURSE_DATE,
                LocalTime.of(17, 0),
                LocalTime.of(18, 0)
        );

        assertThat(scheduledAt).isEqualTo(LocalDateTime.of(2026, 9, 3, 19, 0));
    }

    @Test
    void 계산된_시각이_21시이면_다음날_10시로_이관한다() {
        LocalDateTime scheduledAt = calculator.calculate(
                COURSE_DATE,
                LocalTime.of(19, 0),
                LocalTime.of(20, 0)
        );

        assertThat(scheduledAt).isEqualTo(LocalDateTime.of(2026, 9, 4, 10, 0));
    }

    @Test
    void 계산된_시각이_21시_이전이면_당일에_발송한다() {
        LocalDateTime scheduledAt = calculator.calculate(
                COURSE_DATE,
                LocalTime.of(18, 59),
                LocalTime.of(19, 59)
        );

        assertThat(scheduledAt).isEqualTo(LocalDateTime.of(2026, 9, 3, 20, 59));
    }

    @Test
    void 자정을_넘긴_일정은_시작일의_다음날_10시로_이관한다() {
        LocalDateTime scheduledAt = calculator.calculate(
                COURSE_DATE,
                LocalTime.of(23, 0),
                LocalTime.of(0, 30)
        );

        assertThat(scheduledAt).isEqualTo(LocalDateTime.of(2026, 9, 4, 10, 0));
    }

    @Test
    void 새벽에_계산된_발송_시각은_같은날_10시로_이관한다() {
        LocalDateTime scheduledAt = calculator.calculate(
                COURSE_DATE,
                LocalTime.of(6, 0),
                LocalTime.of(7, 0)
        );

        assertThat(scheduledAt).isEqualTo(LocalDateTime.of(2026, 9, 3, 10, 0));
    }

    @Test
    void 시작과_종료_시간이_같으면_계산할_수_없다() {
        assertThatThrownBy(() -> calculator.calculate(
                COURSE_DATE,
                LocalTime.of(18, 0),
                LocalTime.of(18, 0)
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("일정 시작 시간과 종료 시간은 같을 수 없습니다.");
    }
}
