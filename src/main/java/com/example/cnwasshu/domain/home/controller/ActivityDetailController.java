package com.example.cnwasshu.domain.home.controller;

import com.example.cnwasshu.domain.home.dto.ActivityDetailResponse;
import com.example.cnwasshu.domain.home.service.ActivityDetailService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/activities")
public class ActivityDetailController {

    private final ActivityDetailService activityDetailService;

    @GetMapping("/{activityId}")
    public ResponseEntity<ActivityDetailResponse> getActivityDetail(
            @PathVariable Long activityId
    ) {
        return ResponseEntity.ok(
                activityDetailService.getActivityDetail(activityId)
        );
    }
}