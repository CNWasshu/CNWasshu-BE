package com.example.cnwasshu.domain.home.repository;

import com.example.cnwasshu.domain.home.entity.Activity;
import com.example.cnwasshu.domain.home.entity.ActivityStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
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

    Optional<Activity> findByQrCodeAndDeletedAtIsNull(String qrCode);

    @Query(
            value = """
                    SELECT a.activity_id
                    FROM activity a
                    JOIN region r
                      ON r.region_id = a.region_id
                    LEFT JOIN (
                        SELECT s.activity_id,
                               COUNT(*) AS recommended_count
                        FROM survey s
                        WHERE s.is_recommended = TRUE
                        GROUP BY s.activity_id
                    ) recommendation
                      ON recommendation.activity_id = a.activity_id
                    WHERE a.deleted_at IS NULL
                      AND a.status = 'OPEN'
                      AND (:regionId IS NULL OR a.region_id = :regionId)
                      AND (:categoryId IS NULL OR a.category_id = :categoryId)
                    ORDER BY
                        r.is_depopulated_area DESC,
                        COALESCE(recommendation.recommended_count, 0) DESC,
                        a.activity_id ASC
                    """,
            countQuery = """
                    SELECT COUNT(*)
                    FROM activity a
                    WHERE a.deleted_at IS NULL
                      AND a.status = 'OPEN'
                      AND (:regionId IS NULL OR a.region_id = :regionId)
                      AND (:categoryId IS NULL OR a.category_id = :categoryId)
                    """,
            nativeQuery = true
    )
    Page<Long> findHomeActivityIdsDefault(
            @Param("regionId") Integer regionId,
            @Param("categoryId") Integer categoryId,
            Pageable pageable
    );

    @Query(
            value = """
                    SELECT a.activity_id
                    FROM activity a
                    LEFT JOIN (
                        SELECT s.activity_id,
                               COUNT(*) AS recommended_count
                        FROM survey s
                        WHERE s.is_recommended = TRUE
                        GROUP BY s.activity_id
                    ) recommendation
                      ON recommendation.activity_id = a.activity_id
                    WHERE a.deleted_at IS NULL
                      AND a.status = 'OPEN'
                      AND (:regionId IS NULL OR a.region_id = :regionId)
                      AND (:categoryId IS NULL OR a.category_id = :categoryId)
                    ORDER BY
                        COALESCE(recommendation.recommended_count, 0) DESC,
                        a.activity_id ASC
                    """,
            countQuery = """
                    SELECT COUNT(*)
                    FROM activity a
                    WHERE a.deleted_at IS NULL
                      AND a.status = 'OPEN'
                      AND (:regionId IS NULL OR a.region_id = :regionId)
                      AND (:categoryId IS NULL OR a.category_id = :categoryId)
                    """,
            nativeQuery = true
    )
    Page<Long> findHomeActivityIdsRecommended(
            @Param("regionId") Integer regionId,
            @Param("categoryId") Integer categoryId,
            Pageable pageable
    );

    @Query(
            value = """
                    SELECT a.activity_id
                    FROM activity a
                    LEFT JOIN (
                        SELECT r.activity_id,
                               COUNT(*) AS reservation_count
                        FROM reservation r
                        WHERE r.status = 'CONFIRMED'
                          AND r.deleted_at IS NULL
                        GROUP BY r.activity_id
                    ) reservation_count
                      ON reservation_count.activity_id = a.activity_id
                    WHERE a.deleted_at IS NULL
                      AND a.status = 'OPEN'
                      AND (:regionId IS NULL OR a.region_id = :regionId)
                      AND (:categoryId IS NULL OR a.category_id = :categoryId)
                    ORDER BY
                        COALESCE(reservation_count.reservation_count, 0) DESC,
                        a.activity_id ASC
                    """,
            countQuery = """
                    SELECT COUNT(*)
                    FROM activity a
                    WHERE a.deleted_at IS NULL
                      AND a.status = 'OPEN'
                      AND (:regionId IS NULL OR a.region_id = :regionId)
                      AND (:categoryId IS NULL OR a.category_id = :categoryId)
                    """,
            nativeQuery = true
    )
    Page<Long> findHomeActivityIdsReservation(
            @Param("regionId") Integer regionId,
            @Param("categoryId") Integer categoryId,
            Pageable pageable
    );

    @Query(
            value = """
                    SELECT a.activity_id
                    FROM activity a
                    LEFT JOIN (
                        SELECT b.activity_id,
                               COUNT(*) AS bookmark_count
                        FROM bookmark b
                        WHERE b.activity_id IS NOT NULL
                        GROUP BY b.activity_id
                    ) bookmark_count
                      ON bookmark_count.activity_id = a.activity_id
                    WHERE a.deleted_at IS NULL
                      AND a.status = 'OPEN'
                      AND (:regionId IS NULL OR a.region_id = :regionId)
                      AND (:categoryId IS NULL OR a.category_id = :categoryId)
                    ORDER BY
                        COALESCE(bookmark_count.bookmark_count, 0) DESC,
                        a.activity_id ASC
                    """,
            countQuery = """
                    SELECT COUNT(*)
                    FROM activity a
                    WHERE a.deleted_at IS NULL
                      AND a.status = 'OPEN'
                      AND (:regionId IS NULL OR a.region_id = :regionId)
                      AND (:categoryId IS NULL OR a.category_id = :categoryId)
                    """,
            nativeQuery = true
    )
    Page<Long> findHomeActivityIdsBookmark(
            @Param("regionId") Integer regionId,
            @Param("categoryId") Integer categoryId,
            Pageable pageable
    );

    @EntityGraph(attributePaths = {"region", "category"})
    @Query("""
            SELECT a
            FROM Activity a
            WHERE a.id IN :ids
              AND a.deletedAt IS NULL
            """)
    List<Activity> findAllByIdsWithRegionAndCategory(
            @Param("ids") Collection<Long> ids
    );
}