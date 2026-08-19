package com.example.cnwasshu.domain.course.controller;

import com.example.cnwasshu.domain.course.dto.request.AiCourseRequest;
import com.example.cnwasshu.domain.course.dto.request.CourseRenameRequest;
import com.example.cnwasshu.domain.course.dto.request.CourseSaveRequest;
import com.example.cnwasshu.domain.course.dto.response.AiCourseRecommendResponse;
import com.example.cnwasshu.domain.course.dto.response.CourseDetailResponse;
import com.example.cnwasshu.domain.course.dto.response.CourseSummaryResponse;
import com.example.cnwasshu.domain.course.service.AiCourseRecommendService;
import com.example.cnwasshu.domain.course.service.CourseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * userId는 보민님이 구현하는 로그인/인증(common.security) 완성 전까지는
 * 임시로 요청 헤더나 테스트용 고정값으로 받고, 인증 붙으면
 * @AuthenticationPrincipal 등으로 교체하면 된다. (아래 TODO 참고)
 */
@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;
    private final AiCourseRecommendService aiCourseRecommendService;

    // TODO: 인증 붙으면 @AuthenticationPrincipal CustomUserDetails user 로 교체하고
    // userId = user.getId() 형태로 바꾸기. 지금은 개발 편의를 위해 헤더로 받음.
    private Long resolveUserId(Long userIdHeader) {
        if (userIdHeader == null) {
            throw new IllegalArgumentException("X-USER-ID 헤더가 필요합니다 (임시).");
        }
        return userIdHeader;
    }

    @GetMapping
    public ResponseEntity<List<CourseSummaryResponse>> getMyCourses(
            @RequestHeader("X-USER-ID") Long userIdHeader) {
        Long userId = resolveUserId(userIdHeader);
        return ResponseEntity.ok(courseService.getMyCourses(userId));
    }

    @GetMapping("/{courseId}")
    public ResponseEntity<CourseDetailResponse> getCourseDetail(
            @RequestHeader("X-USER-ID") Long userIdHeader,
            @PathVariable Long courseId) {
        Long userId = resolveUserId(userIdHeader);
        return ResponseEntity.ok(courseService.getCourseDetail(userId, courseId));
    }

    /** 타임테이블(장바구니)에서 짠 코스 저장 */
    @PostMapping
    public ResponseEntity<CourseDetailResponse> saveCourse(
            @RequestHeader("X-USER-ID") Long userIdHeader,
            @Valid @RequestBody CourseSaveRequest request) {
        Long userId = resolveUserId(userIdHeader);
        CourseDetailResponse response = courseService.saveManualCourse(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("/{courseId}")
    public ResponseEntity<Void> renameCourse(
            @RequestHeader("X-USER-ID") Long userIdHeader,
            @PathVariable Long courseId,
            @Valid @RequestBody CourseRenameRequest request) {
        Long userId = resolveUserId(userIdHeader);
        courseService.renameCourse(userId, courseId, request.courseName());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{courseId}")
    public ResponseEntity<Void> deleteCourse(
            @RequestHeader("X-USER-ID") Long userIdHeader,
            @PathVariable Long courseId) {
        Long userId = resolveUserId(userIdHeader);
        courseService.deleteCourse(userId, courseId);
        return ResponseEntity.noContent().build();
    }

    /** AI 추천 미리보기 (저장 안 함) */
    @PostMapping("/ai-recommendations")
    public ResponseEntity<AiCourseRecommendResponse> recommend(
            @Valid @RequestBody AiCourseRequest request) {
        return ResponseEntity.ok(aiCourseRecommendService.recommend(request));
    }

    /** AI 추천 결과를 실제로 저장 (프론트가 recommend 응답의 items를 그대로 담아 보냄) */
    @PostMapping("/ai-recommendations/save")
    public ResponseEntity<CourseDetailResponse> saveAiRecommendation(
            @RequestHeader("X-USER-ID") Long userIdHeader,
            @Valid @RequestBody CourseSaveRequest request) {
        Long userId = resolveUserId(userIdHeader);
        CourseDetailResponse response = courseService.saveAiCourse(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}