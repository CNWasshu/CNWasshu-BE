package com.example.cnwasshu.domain.course.controller;

import com.example.cnwasshu.common.security.CustomUserPrincipal;
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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;
    private final AiCourseRecommendService aiCourseRecommendService;

    @GetMapping
    public ResponseEntity<List<CourseSummaryResponse>> getMyCourses(
            @AuthenticationPrincipal CustomUserPrincipal principal) {
        return ResponseEntity.ok(courseService.getMyCourses(principal.userId()));
    }

    @GetMapping("/{courseId}")
    public ResponseEntity<CourseDetailResponse> getCourseDetail(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable Long courseId) {
        return ResponseEntity.ok(courseService.getCourseDetail(principal.userId(), courseId));
    }

    /** 타임테이블(장바구니)에서 짠 코스 저장 */
    @PostMapping
    public ResponseEntity<CourseDetailResponse> saveCourse(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @Valid @RequestBody CourseSaveRequest request) {
        CourseDetailResponse response = courseService.saveManualCourse(principal.userId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("/{courseId}")
    public ResponseEntity<Void> renameCourse(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable Long courseId,
            @Valid @RequestBody CourseRenameRequest request) {
        courseService.renameCourse(principal.userId(), courseId, request.courseName());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{courseId}")
    public ResponseEntity<Void> deleteCourse(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable Long courseId) {
        courseService.deleteCourse(principal.userId(), courseId);
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
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @Valid @RequestBody CourseSaveRequest request) {
        CourseDetailResponse response = courseService.saveAiCourse(principal.userId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
