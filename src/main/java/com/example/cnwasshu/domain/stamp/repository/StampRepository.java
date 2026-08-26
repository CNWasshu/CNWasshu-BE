package com.example.cnwasshu.domain.stamp.repository;

import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.cnwasshu.domain.stamp.entity.Stamp;

public interface StampRepository extends JpaRepository<Stamp, Long> {

    boolean existsByUserIdAndActivityId(Long userId, Long activityId);

    @EntityGraph(attributePaths = {"activity", "activity.region"})
    List<Stamp> findByUserIdOrderByStampedAtDesc(Long userId);
}
