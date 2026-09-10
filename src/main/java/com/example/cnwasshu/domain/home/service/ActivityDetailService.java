package com.example.cnwasshu.domain.home.service;

import com.example.cnwasshu.domain.home.dto.ActivityDetailResponse;
import com.example.cnwasshu.domain.home.entity.Activity;
import com.example.cnwasshu.domain.home.entity.ActivityImage;
import com.example.cnwasshu.domain.home.repository.ActivityImageRepository;
import com.example.cnwasshu.domain.home.repository.ActivityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ActivityDetailService {

    private final ActivityRepository activityRepository;
    private final ActivityImageRepository activityImageRepository;

    public ActivityDetailResponse getActivityDetail(Long activityId) {

        Activity activity = activityRepository
                .findByIdAndDeletedAtIsNull(activityId)
                .orElseThrow(() ->
                        new IllegalArgumentException("존재하지 않는 체험입니다.")
                );

        List<String> images = activityImageRepository
                .findByActivityIdOrderBySortOrderAsc(activityId)
                .stream()
                .map(ActivityImage::getImageUrl)
                .toList();

        return ActivityDetailResponse.from(activity, images);
    }
}