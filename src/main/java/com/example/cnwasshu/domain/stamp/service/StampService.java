package com.example.cnwasshu.domain.stamp.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.cnwasshu.common.exception.BusinessException;
import com.example.cnwasshu.common.exception.ErrorCode;
import com.example.cnwasshu.domain.home.entity.Activity;
import com.example.cnwasshu.domain.home.repository.ActivityRepository;
import com.example.cnwasshu.domain.stamp.dto.request.StampCreateRequest;
import com.example.cnwasshu.domain.stamp.dto.response.StampListResponse;
import com.example.cnwasshu.domain.stamp.dto.response.StampResponse;
import com.example.cnwasshu.domain.stamp.entity.Stamp;
import com.example.cnwasshu.domain.stamp.repository.StampRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class StampService {

    private final StampRepository stampRepository;
    private final ActivityRepository activityRepository;

    public StampResponse create(Long userId, StampCreateRequest request) {
        Activity activity = activityRepository.findByQrCodeAndDeletedAtIsNull(request.qrCode())
                .orElseThrow(() -> new BusinessException(ErrorCode.STAMP_ACTIVITY_NOT_FOUND));

        if (stampRepository.existsByUserIdAndActivityId(userId, activity.getId())) {
            throw new BusinessException(ErrorCode.STAMP_ALREADY_EXISTS);
        }

        Stamp stamp = stampRepository.save(Stamp.of(userId, activity, request.photo()));
        return StampResponse.from(stamp);
    }

    @Transactional(readOnly = true)
    public StampListResponse getMyStamps(Long userId) {
        return StampListResponse.from(stampRepository.findByUserIdOrderByStampedAtDesc(userId));
    }
}
