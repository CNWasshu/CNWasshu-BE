package com.example.cnwasshu.domain.timetable.controller;

import com.example.cnwasshu.common.security.CustomUserPrincipal;
import com.example.cnwasshu.domain.timetable.dto.request.TimetableSaveRequest;
import com.example.cnwasshu.domain.timetable.dto.response.TimetableDetailResponse;
import com.example.cnwasshu.domain.timetable.dto.response.SavedActivityListResponse;
import com.example.cnwasshu.domain.timetable.service.TimetableService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/timetables")
@RequiredArgsConstructor
public class TimetableController {

    private final TimetableService timetableService;

    @GetMapping("/saved-activities")
    public ResponseEntity<SavedActivityListResponse> getSavedActivities(
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        return ResponseEntity.ok(timetableService.getSavedActivities(principal.userId()));
    }

    @PostMapping
    public ResponseEntity<TimetableDetailResponse> createTimetable(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @Valid @RequestBody TimetableSaveRequest request
    ) {
        TimetableDetailResponse response = timetableService.createTimetable(principal.userId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

}
