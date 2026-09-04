# 온보딩 API

인증된 사용자의 온보딩 상태를 조회하고 완료 또는 건너뛰기 상태를 저장한다.

## 상태 조회

```http
GET /api/users/me/onboarding
Authorization: Bearer {accessToken}
```

응답 예시

```json
{
  "onboardingStatus": "NOT_STARTED",
  "onboardingCompletedAt": null
}
```

## 완료 처리

```http
PATCH /api/users/me/onboarding
Authorization: Bearer {accessToken}
Content-Type: application/json
```

마지막 페이지를 완료한 경우

```json
{
  "completionType": "COMPLETED"
}
```

건너뛴 경우

```json
{
  "completionType": "SKIPPED"
}
```

응답 예시

```json
{
  "onboardingStatus": "COMPLETED",
  "onboardingCompletedAt": "2026-09-04T09:30:00"
}
```

완료 또는 건너뛰기 처리가 끝난 계정에 다시 요청해도 최초 상태와 완료 시각을 유지한다.
