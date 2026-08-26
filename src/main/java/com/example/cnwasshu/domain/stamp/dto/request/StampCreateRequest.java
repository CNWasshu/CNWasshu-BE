package com.example.cnwasshu.domain.stamp.dto.request;

import jakarta.validation.constraints.NotBlank;

public record StampCreateRequest(
        @NotBlank(message = "qrCode는 필수입니다.") String qrCode,
        String photo
) {
}
