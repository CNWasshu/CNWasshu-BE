package com.example.cnwasshu.domain.notification.dto.response;

import java.util.List;

import org.springframework.data.domain.Page;

import com.example.cnwasshu.domain.notification.entity.Notification;

public record NotificationListResponse(
        List<NotificationResponse> notifications,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean hasNext
) {

    public static NotificationListResponse from(Page<Notification> page) {
        return new NotificationListResponse(
                page.getContent().stream().map(NotificationResponse::from).toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.hasNext()
        );
    }
}
