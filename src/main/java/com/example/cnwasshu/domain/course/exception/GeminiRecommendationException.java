package com.example.cnwasshu.domain.course.exception;

public class GeminiRecommendationException extends RuntimeException {
    public GeminiRecommendationException(String message) {
        super(message);
    }

    public GeminiRecommendationException(String message, Throwable cause) {
        super(message, cause);
    }
}
