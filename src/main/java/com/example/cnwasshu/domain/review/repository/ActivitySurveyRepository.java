package com.example.cnwasshu.domain.review.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.cnwasshu.domain.review.entity.ActivitySurvey;

public interface ActivitySurveyRepository extends JpaRepository<ActivitySurvey, Long> {

    List<ActivitySurvey> findAllByCourseSurveyIdOrderByCourseItemId(Long courseSurveyId);

    Optional<ActivitySurvey> findByCourseSurveyIdAndCourseItemId(Long courseSurveyId, Long courseItemId);

    boolean existsByCourseSurveyIdAndCourseItemId(Long courseSurveyId, Long courseItemId);
}
