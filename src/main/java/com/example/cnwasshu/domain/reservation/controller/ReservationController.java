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

    @GetMapping
    public ResponseEntity<List<ReservationResponse>> getReservations(
            @AuthenticationPrincipal CustomUserPrincipal principal,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate startDate,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate endDate
    ) {

        Long userId = principal.userId();

        if (date != null) {

            if (startDate != null || endDate != null) {
                throw new IllegalArgumentException(
                        "date와 startDate/endDate는 함께 사용할 수 없습니다."
                );
            }

            return ResponseEntity.ok(
                    reservationService.getReservationsByDate(
                            userId,
                            date
                    )
            );
        }

        if (startDate != null || endDate != null) {

            if (startDate == null || endDate == null) {
                throw new IllegalArgumentException(
                        "기간 조회 시 startDate와 endDate를 모두 입력해야 합니다."
                );
            }

            return ResponseEntity.ok(
                    reservationService.getReservationsByPeriod(
                            userId,
                            startDate,
                            endDate
                    )
            );
        }

        return ResponseEntity.ok(
                reservationService.getReservations(
                        userId
                )
        );
    }

    @GetMapping("/activities/{activityId}/available-times")
    public ResponseEntity<List<AvailableTimeResponse>> getAvailableTimes(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable Long activityId,

            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date
    ) {
        return ResponseEntity.ok(
                reservationService.getAvailableTimes(
                        principal.userId(),
                        activityId,
                        date
                )
        );
    }
}