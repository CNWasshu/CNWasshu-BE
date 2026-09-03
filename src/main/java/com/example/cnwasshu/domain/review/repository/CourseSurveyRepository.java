package com.example.cnwasshu.domain.review.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.cnwasshu.domain.review.entity.CourseSurvey;
import com.example.cnwasshu.domain.review.entity.SurveyType;

public interface CourseSurveyRepository extends JpaRepository<CourseSurvey, Long> {

    List<CourseSurvey> findAllByCourseId(Long courseId);

    Optional<CourseSurvey> findByIdAndUserId(Long id, Long userId);

    Optional<CourseSurvey> findByUserIdAndCourseIdAndCourseDateAndSurveyType(
            Long userId,
            Long courseId,
            LocalDate courseDate,
            SurveyType surveyType
    );

    boolean existsByUserIdAndCourseIdAndCourseDateAndSurveyType(
            Long userId,
            Long courseId,
            LocalDate courseDate,
            SurveyType surveyType
    );
}
