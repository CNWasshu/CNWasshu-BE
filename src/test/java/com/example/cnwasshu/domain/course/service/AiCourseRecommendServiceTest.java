package com.example.cnwasshu.domain.course.service;

import com.example.cnwasshu.domain.course.dto.request.AiCourseRequest;
import com.example.cnwasshu.domain.course.dto.request.Transportation;
import com.example.cnwasshu.domain.course.dto.request.TravelStyle;
import com.example.cnwasshu.domain.course.dto.response.AiCourseRecommendResponse;
import com.example.cnwasshu.domain.course.dto.response.CourseItemResponse;
import com.example.cnwasshu.domain.course.exception.InvalidTravelPeriodException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

class AiCourseRecommendServiceTest {

    private final GeminiCourseClient geminiCourseClient = mock(GeminiCourseClient.class);
    private final KakaoMobilityDirectionsClient directionsClient = mock(KakaoMobilityDirectionsClient.class);
    private final AiCourseRecommendService service = new AiCourseRecommendService(geminiCourseClient, directionsClient);

    @Test
    void validRequestIsDelegatedToGemini() {
        AiCourseRequest request = request(LocalDate.of(2026, 9, 10), LocalDate.of(2026, 9, 12));
        AiCourseRecommendResponse expected = new AiCourseRecommendResponse("공주 힐링 코스", List.of());
        when(geminiCourseClient.recommend(request)).thenReturn(expected);

        AiCourseRecommendResponse actual = service.recommend(request);

        assertThat(actual).isEqualTo(expected);
        verify(geminiCourseClient).recommend(request);
    }

    @Test
    void addsRoutesOnlyBetweenConsecutiveItemsOnTheSameDay() {
        AiCourseRequest request = request(LocalDate.of(2026, 9, 10), LocalDate.of(2026, 9, 11));
        AiCourseRecommendResponse recommendation = new AiCourseRecommendResponse(
                "공주 힐링 코스",
                List.of(
                        item("첫 장소", 1, 1, "36.1", "127.1"),
                        item("둘째 장소", 1, 2, "36.2", "127.2"),
                        item("다음 날 장소", 2, 1, "36.3", "127.3")
                )
        );
        when(geminiCourseClient.recommend(request)).thenReturn(recommendation);
        when(directionsClient.getCarRoute(
                new BigDecimal("36.1"), new BigDecimal("127.1"),
                new BigDecimal("36.2"), new BigDecimal("127.2")))
                .thenReturn(Optional.of(new KakaoMobilityDirectionsClient.RouteInfo(12_000, 1_080)));

        AiCourseRecommendResponse actual = service.recommend(request);

        assertThat(actual.items().get(0).distanceMeters()).isEqualTo(12_000);
        assertThat(actual.items().get(0).travelTimeSeconds()).isEqualTo(1_080);
        assertThat(actual.items().get(1).distanceMeters()).isNull();
        assertThat(actual.items().get(2).distanceMeters()).isNull();
        verify(directionsClient).getCarRoute(
                new BigDecimal("36.1"), new BigDecimal("127.1"),
                new BigDecimal("36.2"), new BigDecimal("127.2"));
        verifyNoMoreInteractions(directionsClient);
    }

    @Test
    void endDateBeforeStartDateIsRejected() {
        AiCourseRequest request = request(LocalDate.of(2026, 9, 12), LocalDate.of(2026, 9, 10));

        assertThatThrownBy(() -> service.recommend(request))
                .isInstanceOf(InvalidTravelPeriodException.class);
    }

    private AiCourseRequest request(LocalDate startDate, LocalDate endDate) {
        return new AiCourseRequest(
                startDate,
                endDate,
                "공주시",
                2,
                Transportation.CAR,
                TravelStyle.HEALING
        );
    }

    private CourseItemResponse item(
            String title, int dayNo, int sortOrder, String latitude, String longitude
    ) {
        return new CourseItemResponse(
                null, null, null, null, title, dayNo,
                LocalTime.of(9 + sortOrder, 0), LocalTime.of(10 + sortOrder, 0), sortOrder,
                null, new BigDecimal(latitude), new BigDecimal(longitude), null, null, null
        );
    }
}
