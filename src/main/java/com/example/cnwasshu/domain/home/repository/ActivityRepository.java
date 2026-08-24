package com.example.cnwasshu.domain.home.repository;

import com.example.cnwasshu.domain.home.entity.Activity;
import com.example.cnwasshu.domain.home.entity.ActivityStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ActivityRepository extends JpaRepository<Activity, Long> {

    List<Activity> findByStatusAndDeletedAtIsNull(ActivityStatus status);

    List<Activity> findByRegionIdAndStatusAndDeletedAtIsNull(
            Integer regionId,
            ActivityStatus status
    );

    List<Activity> findByCategoryIdAndStatusAndDeletedAtIsNull(
            Integer categoryId,
            ActivityStatus status
    );

    List<Activity> findByRegionIdAndCategoryIdAndStatusAndDeletedAtIsNull(
            Integer regionId,
            Integer categoryId,
            ActivityStatus status
    );

    List<Activity> findByDeletedAtIsNull();

    Optional<Activity> findByIdAndDeletedAtIsNull(Long id);
}