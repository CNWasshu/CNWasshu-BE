package com.example.cnwasshu.domain.course.service;

import java.time.LocalTime;

/**
 * AI 추천에 쓰이는 액티비티 후보 정보.
 * activity 도메인(유진/home 쪽)의 실제 엔티티를 그대로 노출하지 않고,
 * 추천 로직에 필요한 필드만 뽑아온 값 객체.
 */
public record ActivityCandidate(
        Long activityId,
        String title,
        Integer regionId,
        Integer categoryId,
        Integer durationMinutes,
        LocalTime operatingStartTime,
        LocalTime operatingEndTime,
        Boolean reservationRequired
) {}