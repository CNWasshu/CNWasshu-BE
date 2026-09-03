package com.example.cnwasshu.domain.review.entity;

import com.example.cnwasshu.common.entity.BaseTimeEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "survey", uniqueConstraints = @UniqueConstraint(
        name = "uk_survey_course_item",
        columnNames = {"course_survey_id", "course_item_id"}
))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ActivitySurvey extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "survey_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_survey_id", nullable = false)
    private CourseSurvey courseSurvey;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "course_item_id", nullable = false)
    private Long courseItemId;

    @Column(name = "activity_id", nullable = false)
    private Long activityId;

    @Column(name = "stamp_id")
    private Long stampId;

    @Column(name = "reservation_id")
    private Long reservationId;

    @Enumerated(EnumType.STRING)
    @Column(name = "visit_status", nullable = false, length = 20)
    private VisitStatus visitStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "visit_evidence_type", nullable = false, length = 20)
    private VisitEvidenceType visitEvidenceType;

    @Column(name = "is_recommended")
    private Boolean recommended;

    @Column(name = "satisfaction_score")
    private Integer satisfactionScore;

    @Column(name = "reason_tags", columnDefinition = "JSON")
    private String reasonTags;

    @Column(name = "comment", length = 300)
    private String comment;

    private ActivitySurvey(
            CourseSurvey courseSurvey,
            Long userId,
            Long courseItemId,
            Long activityId,
            Long stampId,
            Long reservationId,
            VisitStatus visitStatus,
            VisitEvidenceType visitEvidenceType
    ) {
        this.courseSurvey = courseSurvey;
        this.userId = userId;
        this.courseItemId = courseItemId;
        this.activityId = activityId;
        this.stampId = stampId;
        this.reservationId = reservationId;
        this.visitStatus = visitStatus;
        this.visitEvidenceType = visitEvidenceType;
    }

    public static ActivitySurvey create(
            CourseSurvey courseSurvey,
            Long userId,
            Long courseItemId,
            Long activityId,
            Long stampId,
            Long reservationId,
            VisitStatus visitStatus,
            VisitEvidenceType visitEvidenceType
    ) {
        return new ActivitySurvey(
                courseSurvey,
                userId,
                courseItemId,
                activityId,
                stampId,
                reservationId,
                visitStatus,
                visitEvidenceType
        );
    }

    public void updateDraft(
            VisitStatus visitStatus,
            Boolean recommended,
            Integer satisfactionScore,
            String reasonTags,
            String comment
    ) {
        if (visitStatus != null) {
            this.visitStatus = visitStatus;
        }
        if (recommended != null) {
            this.recommended = recommended;
        }
        if (satisfactionScore != null) {
            this.satisfactionScore = satisfactionScore;
        }
        if (reasonTags != null) {
            this.reasonTags = reasonTags;
        }
        if (comment != null) {
            this.comment = comment;
        }
    }
}
