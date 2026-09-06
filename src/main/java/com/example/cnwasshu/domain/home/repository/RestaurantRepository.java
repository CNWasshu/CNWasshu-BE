package com.example.cnwasshu.domain.home.repository;

import com.example.cnwasshu.domain.home.entity.Restaurant;
import com.example.cnwasshu.domain.home.entity.RestaurantStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {

    List<Restaurant> findByStatusAndDeletedAtIsNull(RestaurantStatus status);

    List<Restaurant> findByRegion_IdAndStatusAndDeletedAtIsNull(
            Integer regionId,
            RestaurantStatus status
    );

    List<Restaurant> findByCategory_IdAndStatusAndDeletedAtIsNull(
            Integer categoryId,
            RestaurantStatus status
    );

    List<Restaurant> findByRegion_IdAndCategory_IdAndStatusAndDeletedAtIsNull(
            Integer regionId,
            Integer categoryId,
            RestaurantStatus status
    );

    List<Restaurant> findByDeletedAtIsNull();

    Optional<Restaurant> findByIdAndDeletedAtIsNull(Long id);

    @Query(
            value = """
                    SELECT r.restaurant_id
                    FROM restaurant r
                    WHERE r.deleted_at IS NULL
                      AND r.status = 'OPEN'
                    ORDER BY
                        r.name ASC,
                        r.restaurant_id ASC
                    """,
            countQuery = """
                    SELECT COUNT(*)
                    FROM restaurant r
                    WHERE r.deleted_at IS NULL
                      AND r.status = 'OPEN'
                    """,
            nativeQuery = true
    )
    Page<Long> findHomeRestaurantIdsName(Pageable pageable);

    @Query(
            value = """
                    SELECT r.restaurant_id
                    FROM restaurant r
                    LEFT JOIN (
                        SELECT b.restaurant_id,
                               COUNT(*) AS bookmark_count
                        FROM bookmark b
                        WHERE b.restaurant_id IS NOT NULL
                        GROUP BY b.restaurant_id
                    ) bookmark_count
                      ON bookmark_count.restaurant_id = r.restaurant_id
                    WHERE r.deleted_at IS NULL
                      AND r.status = 'OPEN'
                    ORDER BY
                        COALESCE(bookmark_count.bookmark_count, 0) DESC,
                        r.name ASC,
                        r.restaurant_id ASC
                    """,
            countQuery = """
                    SELECT COUNT(*)
                    FROM restaurant r
                    WHERE r.deleted_at IS NULL
                      AND r.status = 'OPEN'
                    """,
            nativeQuery = true
    )
    Page<Long> findHomeRestaurantIdsBookmark(Pageable pageable);

    @EntityGraph(attributePaths = {"region", "category"})
    @Query("""
            SELECT r
            FROM Restaurant r
            WHERE r.id IN :ids
              AND r.deletedAt IS NULL
            """)
    List<Restaurant> findAllByIdsWithRegionAndCategory(
            @Param("ids") Collection<Long> ids
    );
}