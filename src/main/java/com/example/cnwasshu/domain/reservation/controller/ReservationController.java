package com.example.cnwasshu.domain.reservation.controller;

import com.example.cnwasshu.common.security.CustomUserPrincipal;
import com.example.cnwasshu.domain.reservation.dto.AvailableTimeResponse;
import com.example.cnwasshu.domain.reservation.dto.ReservationCreateRequest;
import com.example.cnwasshu.domain.reservation.dto.ReservationResponse;
import com.example.cnwasshu.domain.reservation.service.ReservationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/reservations")
public class ReservationController {

    private final ReservationService reservationService;

    @PostMapping
    public ResponseEntity<ReservationResponse> createReservation(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @Valid @RequestBody ReservationCreateRequest request
    ) {
        return ResponseEntity.ok(
                reservationService.createReservation(
                        principal.userId(),
                        request
                )
        );
    }

    @GetMapping("/activities/{activityId}/available-times")
    public ResponseEntity<List<AvailableTimeResponse>> getAvailableTimes(
            @PathVariable Long activityId,
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date
    ) {
        return ResponseEntity.ok(
                reservationService.getAvailableTimes(
                        activityId,
                        date
                )
        );
    }
}