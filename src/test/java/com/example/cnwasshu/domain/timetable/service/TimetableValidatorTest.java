package com.example.cnwasshu.domain.timetable.service;

import com.example.cnwasshu.domain.timetable.dto.request.TimetableDayRequest;
import com.example.cnwasshu.domain.timetable.dto.request.TimetableSaveRequest;
import com.example.cnwasshu.domain.timetable.dto.request.TimetableScheduleRequest;
import com.example.cnwasshu.domain.timetable.entity.ActivityOperatingType;
import com.example.cnwasshu.domain.timetable.entity.TimetableScheduleType;
import com.example.cnwasshu.domain.timetable.exception.RestaurantOutsideOperatingHoursException;
import com.example.cnwasshu.domain.timetable.exception.TimetableRestaurantNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TimetableValidatorTest {

    private static final Long RESTAURANT_ID = 31L;
    private static final LocalDate TRAVEL_DATE = LocalDate.of(2026, 8, 26);

    @Mock
    private TimetableReferenceQueryPort referenceQueryPort;

    @InjectMocks
    private TimetableValidator timetableValidator;

    @Test
    void acceptsRestaurantScheduleWithinOperatingHours() {
        stubEmptyActivityAndReservationReferences();
        when(referenceQueryPort.findRestaurants(Set.of(RESTAURANT_ID)))
                .thenReturn(Map.of(
                        RESTAURANT_ID,
                        new TimetableRestaurantInfo(
                                RESTAURANT_ID,
                                ActivityOperatingType.HOURS,
                                LocalTime.of(11, 0),
                                LocalTime.of(21, 0)
                        )
                ));

        assertThatCode(() -> timetableValidator.validate(
                1L,
                restaurantRequest(LocalTime.of(12, 0), LocalTime.of(13, 0))
        )).doesNotThrowAnyException();
    }

    @Test
    void rejectsRestaurantScheduleWhenRestaurantDoesNotExist() {
        stubEmptyActivityAndReservationReferences();
        when(referenceQueryPort.findRestaurants(Set.of(RESTAURANT_ID)))
                .thenReturn(Collections.emptyMap());

        assertThatThrownBy(() -> timetableValidator.validate(
                1L,
                restaurantRequest(LocalTime.of(12, 0), LocalTime.of(13, 0))
        )).isInstanceOf(TimetableRestaurantNotFoundException.class);
    }

    @Test
    void rejectsRestaurantScheduleOutsideOperatingHours() {
        stubEmptyActivityAndReservationReferences();
        when(referenceQueryPort.findRestaurants(Set.of(RESTAURANT_ID)))
                .thenReturn(Map.of(
                        RESTAURANT_ID,
                        new TimetableRestaurantInfo(
                                RESTAURANT_ID,
                                ActivityOperatingType.HOURS,
                                LocalTime.of(11, 0),
                                LocalTime.of(21, 0)
                        )
                ));

        assertThatThrownBy(() -> timetableValidator.validate(
                1L,
                restaurantRequest(LocalTime.of(10, 0), LocalTime.of(11, 0))
        )).isInstanceOf(RestaurantOutsideOperatingHoursException.class);
    }

    private void stubEmptyActivityAndReservationReferences() {
        when(referenceQueryPort.findActivities(Collections.emptySet()))
                .thenReturn(Collections.emptyMap());
        when(referenceQueryPort.findReservations(Collections.emptySet()))
                .thenReturn(Collections.emptyMap());
    }

    private TimetableSaveRequest restaurantRequest(LocalTime startTime, LocalTime endTime) {
        TimetableScheduleRequest schedule = new TimetableScheduleRequest(
                "restaurant-31",
                TimetableScheduleType.RESTAURANT,
                null,
                RESTAURANT_ID,
                null,
                "서산 맛집",
                startTime,
                endTime,
                null,
                1
        );

        return new TimetableSaveRequest(
                "충남 맛집 여행",
                TRAVEL_DATE,
                TRAVEL_DATE,
                List.of(new TimetableDayRequest(
                        1,
                        TRAVEL_DATE,
                        List.of(schedule)
                ))
        );
    }
}
