package com.example.cnwasshu.domain.course.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "course_item")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CourseItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "course_item_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    // 다른 도메인(activity, reservation) 엔티티를 직접 참조하지 않고 ID만 보관.
    // 상세 정보가 필요하면 서비스 계층에서 activity 도메인 조회 API/레포지토리를 호출한다.
    @Column(name = "activity_id")
    private Long activityId;

    @Column(name = "reservation_id")
    private Long reservationId;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(name = "day_no", nullable = false)
    private Integer dayNo;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Column(columnDefinition = "TEXT")
    private String memo;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Builder
    public CourseItem(Long activityId, Long reservationId, String title, Integer dayNo,
                      LocalTime startTime, LocalTime endTime, String memo, Integer sortOrder) {
        this.activityId = activityId;
        this.reservationId = reservationId;
        this.title = title;
        this.dayNo = dayNo;
        this.startTime = startTime;
        this.endTime = endTime;
        this.memo = memo;
        this.sortOrder = sortOrder;
    }

    void assignCourse(Course course) {
        this.course = course;
    }

    /** 같은 날짜(dayNo) 안에서 시간이 겹치는지 확인 */
    public boolean overlapsWith(CourseItem other) {
        if (!this.dayNo.equals(other.dayNo)) return false;
        return this.startTime.isBefore(other.endTime) && other.startTime.isBefore(this.endTime);
    }
}