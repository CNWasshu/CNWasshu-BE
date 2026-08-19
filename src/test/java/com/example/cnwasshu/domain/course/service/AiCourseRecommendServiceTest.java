package com.example.cnwasshu.domain.course.service;

import com.example.cnwasshu.domain.course.dto.request.AiCourseRequest;
import com.example.cnwasshu.domain.course.dto.request.Transportation;
import com.example.cnwasshu.domain.course.dto.request.TravelStyle;
import com.example.cnwasshu.domain.course.dto.response.AiCourseRecommendResponse;
import com.example.cnwasshu.domain.course.exception.InvalidTravelPeriodException;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AiCourseRecommendServiceTest {

    private final GeminiCourseClient geminiCourseClient = mock(GeminiCourseClient.class);
    private final AiCourseRecommendService service = new AiCourseRecommendService(geminiCourseClient);

    @Test
    void validRequestIsDelegatedToGemini() {
        AiCourseRequest request = request(LocalDate.of(2026, 9, 10), LocalDate.of(2026, 9, 12));
        AiCourseRecommendResponse expected = new AiCourseRecommendResponse("공주 힐링 코스", List.of());
        when(geminiCourseClient.recommend(request)).thenReturn(expected);

        AiCourseRecommendResponse actual = service.recommend(request);

        assertThat(actual).isSameAs(expected);
        verify(geminiCourseClient).recommend(request);
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
}
