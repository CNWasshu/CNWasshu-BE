package com.example.cnwasshu.domain.course.service;

import com.example.cnwasshu.domain.course.dto.request.AiCourseRequest;
import com.example.cnwasshu.domain.course.dto.response.AiCourseRecommendResponse;
import com.example.cnwasshu.domain.course.dto.response.CourseItemResponse;
import com.example.cnwasshu.domain.course.exception.InvalidTravelPeriodException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AiCourseRecommendService {

    private final GeminiCourseClient geminiCourseClient;
    private final KakaoMobilityDirectionsClient directionsClient;

    public AiCourseRecommendResponse recommend(AiCourseRequest request) {
        if (request.endDate().isBefore(request.startDate())) {
            throw new InvalidTravelPeriodException();
        }
        AiCourseRecommendResponse recommendation = geminiCourseClient.recommend(request);
        return new AiCourseRecommendResponse(
                recommendation.suggestedCourseName(),
                addRoutesToNextItems(recommendation.items())
        );
    }

    private List<CourseItemResponse> addRoutesToNextItems(List<CourseItemResponse> items) {
        List<CourseItemResponse> enriched = new ArrayList<>(items);
        for (int index = 0; index < items.size() - 1; index++) {
            CourseItemResponse current = items.get(index);
            CourseItemResponse next = items.get(index + 1);
            if (!current.dayNo().equals(next.dayNo())
                    || current.latitude() == null || current.longitude() == null
                    || next.latitude() == null || next.longitude() == null) {
                continue;
            }
            var route = directionsClient.getCarRoute(
                    current.latitude(), current.longitude(), next.latitude(), next.longitude());
            if (route.isPresent()) {
                enriched.set(index, current.withRouteToNext(
                        route.get().distanceMeters(), route.get().travelTimeSeconds()));
            }
        }
        return List.copyOf(enriched);
    }
}
