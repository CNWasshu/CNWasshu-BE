package com.example.cnwasshu.domain.home.repository;

import com.example.cnwasshu.domain.home.entity.ActivityWeather;
import com.example.cnwasshu.domain.home.entity.ActivityWeatherId;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface ActivityWeatherRepository extends JpaRepository<ActivityWeather, ActivityWeatherId> {

    List<ActivityWeather> findByActivity_Id(Long activityId);

    @EntityGraph(attributePaths = {"activity", "weatherTag"})
    List<ActivityWeather> findByActivity_IdIn(Collection<Long> activityIds);
}