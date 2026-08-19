package com.example.cnwasshu.domain.course.service;

import com.example.cnwasshu.domain.course.dto.request.AiCourseRequest;
import com.example.cnwasshu.domain.course.dto.response.AiCourseRecommendResponse;
import com.example.cnwasshu.domain.course.exception.InvalidTravelPeriodException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AiCourseRecommendService {

    private final GeminiCourseClient geminiCourseClient;

    public AiCourseRecommendResponse recommend(AiCourseRequest request) {
        if (request.endDate().isBefore(request.startDate())) {
            throw new InvalidTravelPeriodException();
        }
        return geminiCourseClient.recommend(request);
    }
}
