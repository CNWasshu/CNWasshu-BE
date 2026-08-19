package com.example.cnwasshu.domain.course.service.adapter;

import com.example.cnwasshu.domain.course.service.ActivityCandidate;
import com.example.cnwasshu.domain.course.service.ActivityQueryPort;
import org.springframework.stereotype.Component;

import java.time.LocalTime;
import java.util.List;

/**
 * 임시 스텁 구현체.
 * activity 도메인(유진님 쪽 Activity 엔티티/레포지토리)이 완성되면
 * 이 클래스를 지우고 실제 ActivityRepository를 사용하는 어댑터로 교체하면 된다.
 *
 * 지금은 서버 기동 및 AI 추천 API 흐름 테스트용으로 더미 후보 몇 개만 반환한다.
 */
@Component
public class StubActivityQueryAdapter implements ActivityQueryPort {

    @Override
    public List<ActivityCandidate> findCandidates(Integer regionId, String travelStyle) {
        return List.of(
                new ActivityCandidate(
                        1L, "공주 한옥마을 체험", regionId, 1,
                        90, LocalTime.of(9, 0), LocalTime.of(18, 0), false
                ),
                new ActivityCandidate(
                        2L, "부여 백제문화단지 투어", regionId, 1,
                        120, LocalTime.of(9, 0), LocalTime.of(17, 0), true
                ),
                new ActivityCandidate(
                        3L, "서산 갯벌 체험", regionId, 2,
                        60, LocalTime.of(10, 0), LocalTime.of(16, 0), false
                )
        );
    }
}