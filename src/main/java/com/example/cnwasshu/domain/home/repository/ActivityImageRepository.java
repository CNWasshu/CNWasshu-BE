package com.example.cnwasshu.domain.home.repository;

import com.example.cnwasshu.domain.home.entity.ActivityImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ActivityImageRepository extends JpaRepository<ActivityImage, Long> {

    List<ActivityImage> findByActivityIdOrderBySortOrderAsc(Long activityId);
}