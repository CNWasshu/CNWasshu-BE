package com.example.cnwasshu.domain.course.repository;

import com.example.cnwasshu.domain.course.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CourseRepository extends JpaRepository<Course, Long> {

    List<Course> findByUserIdAndDeletedAtIsNullOrderByCreatedAtDesc(Long userId);

    Optional<Course> findByIdAndUserIdAndDeletedAtIsNull(Long id, Long userId);
}