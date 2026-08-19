package com.example.cnwasshu.domain.course.dto.response;

import java.util.List;

/**
 * AI 추천 미리보기 응답. 아직 DB에 저장되지 않은 상태이며,
 * 사용자가 "추천 코스 저장"을 누르면 프론트가 이 items를 그대로
 * CourseSaveRequest.items에 담아 /api/courses/ai-recommendations/save 로 보낸다.
 */
public record AiCourseRecommendResponse(
        String suggestedCourseName,
        List<CourseItemResponse> items
) {}