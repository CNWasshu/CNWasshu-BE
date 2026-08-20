package com.example.cnwasshu.domain.course.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "course")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "course_id")
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "course_name", nullable = false, length = 100)
    private String courseName;

    @Enumerated(EnumType.STRING)
    @Column(name = "course_type", nullable = false, length = 10)
    private CourseType courseType;

    @Column(name = "people_count", nullable = false)
    private Integer peopleCount;

    @Column(name = "with_child", nullable = false)
    private Boolean withChild;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    // 코스 안의 일정들. 코스가 삭제되면 일정도 함께 삭제(orphanRemoval)
    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("dayNo ASC, sortOrder ASC, startTime ASC")
    private List<CourseItem> items = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Builder
    public Course(Long userId, String courseName, CourseType courseType,
                  Integer peopleCount, Boolean withChild, LocalDate startDate, LocalDate endDate) {
        this.userId = userId;
        this.courseName = courseName;
        this.courseType = courseType;
        this.peopleCount = peopleCount;
        this.withChild = withChild;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public void rename(String newName) {
        this.courseName = newName;
    }

    public void updateTimetable(
            String courseName,
            Integer peopleCount,
            Boolean withChild,
            LocalDate startDate,
            LocalDate endDate
    ) {
        this.courseName = courseName;
        this.peopleCount = peopleCount;
        this.withChild = withChild;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public void softDelete() {
        this.deletedAt = LocalDateTime.now();
    }

    public boolean isDeleted() {
        return this.deletedAt != null;
    }

    public void addItem(CourseItem item) {
        items.add(item);
        item.assignCourse(this);
    }

    /** 기존 일정을 전부 지우고 새 일정 목록으로 교체 (저장 시 사용) */
    public void replaceItems(List<CourseItem> newItems) {
        this.items.clear();
        newItems.forEach(this::addItem);
    }
}
