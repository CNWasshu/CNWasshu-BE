package com.example.cnwasshu.domain.review.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.example.cnwasshu.common.entity.BaseTimeEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "course_survey", uniqueConstraints = @UniqueConstraint(
        name = "uk_course_survey_target",
        columnNames = {"user_id", "course_id", "course_date", "survey_type"}
))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CourseSurvey extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "course_survey_id")
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "course_id", nullable = false)
    private Long courseId;

    @Column(name = "course_date", nullable = false)
    private LocalDate courseDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "survey_type", nullable = false, length = 20)
    private SurveyType surveyType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private SurveyStatus status;

    @Column(name = "scheduled_at", nullable = false)
    private LocalDateTime scheduledAt;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    @Column(name = "opened_at")
    private LocalDateTime openedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "reminder_count", nullable = false)
    private int reminderCount;

    @Enumerated(EnumType.STRING)
    @Column(name = "course_usage_status", length = 20)
    private CourseUsageStatus courseUsageStatus;

    @Column(name = "overall_score")
    private Integer overallScore;

    @Enumerated(EnumType.STRING)
    @Column(name = "course_pace", length = 20)
    private CoursePace coursePace;

    @Column(name = "issue_tags", columnDefinition = "JSON")
    private String issueTags;

    @Column(name = "not_used_reason_tags", columnDefinition = "JSON")
    private String notUsedReasonTags;

    @Column(name = "comment", length = 300)
    private String comment;

    private CourseSurvey(
            Long userId,
            Long courseId,
            LocalDate courseDate,
            SurveyType surveyType,
            LocalDateTime scheduledAt
    ) {
        this.userId = userId;
        this.courseId = courseId;
        this.courseDate = courseDate;
        this.surveyType = surveyType;
        this.status = SurveyStatus.SCHEDULED;
        this.scheduledAt = scheduledAt;
        this.reminderCount = 0;
    }

    public static CourseSurvey schedule(
            Long userId,
            Long courseId,
            LocalDate courseDate,
            SurveyType surveyType,
            LocalDateTime scheduledAt
    ) {
        return new CourseSurvey(userId, courseId, courseDate, surveyType, scheduledAt);
    }

    public void updateDraft(
            CourseUsageStatus courseUsageStatus,
            Integer overallScore,
            CoursePace coursePace,
            String issueTags,
            String notUsedReasonTags,
            String comment
    ) {
        if (courseUsageStatus != null) {
            this.courseUsageStatus = courseUsageStatus;
        }
        if (overallScore != null) {
            this.overallScore = overallScore;
        }
        if (coursePace != null) {
            this.coursePace = coursePace;
        }
        if (issueTags != null) {
            this.issueTags = issueTags;
        }
        if (notUsedReasonTags != null) {
            this.notUsedReasonTags = notUsedReasonTags;
        }
        if (comment != null) {
            this.comment = comment;
        }
        this.status = SurveyStatus.IN_PROGRESS;
    }

    public boolean isEditable() {
        return status != SurveyStatus.COMPLETED
                && status != SurveyStatus.NOT_USED
                && status != SurveyStatus.EXPIRED
                && status != SurveyStatus.CANCELED;
    }
}
