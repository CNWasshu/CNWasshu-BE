package com.example.cnwasshu.domain.bookmark.repository;

import com.example.cnwasshu.domain.bookmark.entity.Bookmark;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {

    boolean existsByUser_IdAndActivity_Id(Long userId, Long activityId);

    boolean existsByUser_IdAndRestaurant_Id(Long userId, Long restaurantId);

    Optional<Bookmark> findByUser_IdAndActivity_Id(Long userId, Long activityId);

    Optional<Bookmark> findByUser_IdAndRestaurant_Id(Long userId, Long restaurantId);

    List<Bookmark> findAllByUser_IdOrderByCreatedAtDesc(Long userId);
}