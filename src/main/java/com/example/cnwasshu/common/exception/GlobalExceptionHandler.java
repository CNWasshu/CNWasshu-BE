package com.example.cnwasshu.common.exception;

import com.example.cnwasshu.domain.course.exception.CourseNotFoundException;
import com.example.cnwasshu.domain.course.exception.CourseTimeOverlapException;
import com.example.cnwasshu.domain.course.exception.GeminiRecommendationException;
import com.example.cnwasshu.domain.course.exception.InvalidTravelPeriodException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CourseNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNotFound(CourseNotFoundException exception) {
        return response(HttpStatus.NOT_FOUND, "COURSE_NOT_FOUND", exception.getMessage());
    }

    @ExceptionHandler({CourseTimeOverlapException.class, InvalidTravelPeriodException.class,
            IllegalArgumentException.class})
    public ResponseEntity<ApiErrorResponse> handleBadRequest(RuntimeException exception) {
        return response(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", exception.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(MethodArgumentNotValidException exception) {
        String message = exception.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .orElse("요청 값이 올바르지 않습니다.");
        return response(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", message);
    }

    @ExceptionHandler(GeminiRecommendationException.class)
    public ResponseEntity<ApiErrorResponse> handleGemini(GeminiRecommendationException exception) {
        return response(HttpStatus.BAD_GATEWAY, "GEMINI_API_ERROR", exception.getMessage());
    }

    private ResponseEntity<ApiErrorResponse> response(HttpStatus status, String code, String message) {
        return ResponseEntity.status(status).body(new ApiErrorResponse(code, message));
    }
}
