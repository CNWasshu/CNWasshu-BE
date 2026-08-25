package com.example.cnwasshu.domain.reservation.dto;

import java.time.LocalTime;

public record AvailableTimeResponse(
        LocalTime time,
        boolean available
) {
}