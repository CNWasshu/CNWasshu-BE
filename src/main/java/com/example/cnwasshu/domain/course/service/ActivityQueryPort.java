package com.example.cnwasshu.domain.course.service;

import java.util.List;

/**
 * 코스 도메인이 activity 도메인에 의존하지 않고 추천 후보를 가져오기 위한 포트.
 * 실제 구현체(Adapter)는 activity/home 도메인 쪽 Repository를 감싸서
 * infra 패키지 등에서 구현해 Bean으로 등록하면 된다. (팀원과 인터페이스만 먼저 합의)
 *
 * 예시 구현:
 * <pre>
 * {@literal @}Component
 * class ActivityQueryAdapter implements ActivityQueryPort {
 *     private final ActivityRepository activityRepository; // 실제 activity 도메인 repo
 *     public List&lt;ActivityCandidate&gt; findCandidates(Integer regionId, String travelStyle) {
 *         return activityRepository.findByRegionIdAndStatus(regionId, "OPEN")
 *                 .stream().map(a -&gt; new ActivityCandidate(...)).toList();
 *     }
 * }
 * </pre>
 */
public interface ActivityQueryPort {
    List<ActivityCandidate> findCandidates(Integer regionId, String travelStyle);
}