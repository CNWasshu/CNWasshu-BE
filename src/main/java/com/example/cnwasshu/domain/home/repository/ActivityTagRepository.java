package com.example.cnwasshu.domain.home.repository;

import com.example.cnwasshu.domain.home.entity.ActivityTag;
import com.example.cnwasshu.domain.home.entity.ActivityTagId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ActivityTagRepository extends JpaRepository<ActivityTag, ActivityTagId> {

    List<ActivityTag> findByActivity_Id(Long activityId);
}