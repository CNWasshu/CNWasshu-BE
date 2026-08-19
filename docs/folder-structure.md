# 프로젝트 폴더 구조 (CNWasshu)

## 백엔드 구조 (CNWasshu-BE)

```
CNWasshu-BE/
┣ src/main/java/com/example/cnwasshu/
┃  ┣ 📂 common/                 # 공통 유틸, 예외 처리, 보안(Security), Interceptor 등
┃  ┣ 📂 domain/                 # 도메인별 패키지 분리
┃  ┃  ┣ 📂 user/                # 보민 담당: 로그인(마이페이지)
┃  ┃  ┃  ┣ controller/
┃  ┃  ┃  ┣ service/
┃  ┃  ┃  ┣ repository/
┃  ┃  ┃  ┗ entity/
┃  ┃  ┣ 📂 stamp/               # 보민 담당: 스탬프
┃  ┃  ┣ 📂 notification/        # 보민 담당: 알림함
┃  ┃  ┣ 📂 course/              # 현진 담당: 코스 API·AI 연동
┃  ┃  ┣ 📂 reservation/         # 유진 담당: 예약하기
┃  ┃  ┣ 📂 home/                # 유진 담당: 홈 화면 데이터
┃  ┃  ┣ 📂 timetable/           # 예빈 담당: 타임테이블 (여행테이블)
┃  ┃  ┗ 📂 review/              # 예빈 담당: 만족도조사 및 리뷰
┃  ┣ 📜 CnwasshuApplication.java
┗ src/main/resources/
┣ 📜 application.yml
┗ 📜 application-local.yml
```

각 도메인 하위 폴더에 `controller`, `service`, `repository`, `entity`를 각자 만들어서 작업한다.

### 담당 도메인 요약

| 담당자 | 도메인 |
| --- | --- |
| 보민 | user (로그인/마이페이지), stamp (스탬프), notification (알림함) |
| 현진 | course (코스 API, AI 연동) |
| 유진 | reservation (예약), home (홈 화면 데이터) |
| 예빈 | timetable (타임테이블/여행테이블), review (만족도조사, 리뷰) |

---

## 프론트엔드 구조 (CNWasshu-FE)

```
CNWasshu-FE/
┣ 📂 app/                              # 화면 + 라우팅 (Expo Router)
┃ ┣ 📜 _layout.tsx                     # 전체 네비게이션 설정
┃ ┣ 📜 index.tsx                       # 홈 화면
┃ ┃
┃ ┣ 📂 auth/                           # 보민: 로그인/마이페이지
┃ ┃ ┣ 📜 login.tsx
┃ ┃ ┣ 📜 signup.tsx
┃ ┃ ┗ 📜 mypage.tsx
┃ ┃
┃ ┣ 📂 activity/                       # 유진: 충남 체험 백과사전
┃ ┃ ┣ 📜 index.tsx                     # 체험 목록
┃ ┃ ┣ 📜 [id].tsx                      # 체험 상세
┃ ┃ ┗ 📜 search.tsx                    # 체험 검색/필터
┃ ┃
┃ ┣ 📂 reservation/                    # 유진: 예약
┃ ┃ ┣ 📜 index.tsx                     # 예약 화면
┃ ┃ ┣ 📜 [id].tsx                      # 예약 상세
┃ ┃ ┗ 📜 complete.tsx                  # 예약 완료
┃ ┃
┃ ┣ 📂 timetable/                      # 예빈: 여행 테이블
┃ ┃ ┣ 📜 index.tsx
┃ ┃ ┣ 📜 create.tsx
┃ ┃ ┗ 📜 [id].tsx
┃ ┃
┃ ┣ 📂 survey/                         # 예빈: 만족도 조사
┃ ┃ ┣ 📜 index.tsx
┃ ┃ ┗ 📜 result.tsx
┃ ┃
┃ ┣ 📂 course/                         # 현진: 코스 추천 + AI
┃ ┃ ┣ 📜 index.tsx
┃ ┃ ┣ 📜 [id].tsx
┃ ┃ ┗ 📜 ai.tsx
┃ ┃
┃ ┣ 📂 stamp/                          # 보민: 스탬프 투어
┃ ┃ ┣ 📜 index.tsx
┃ ┃ ┣ 📜 [id].tsx
┃ ┃ ┗ 📜 archive.tsx
┃ ┃
┃ ┗ 📂 notification/                   # 보민: 알림
┃   ┣ 📜 index.tsx
┃   ┗ 📜 [id].tsx
┃
┣ 📂 components/                       # 여러 화면에서 재사용
┃ ┣ 📜 ActivityCard.tsx
┃ ┣ 📜 CourseCard.tsx
┃ ┣ 📜 ReservationCard.tsx
┃ ┣ 📜 StampCard.tsx
┃ ┣ 📜 WeatherBadge.tsx
┃ ┣ 📜 Timeline.tsx
┃ ┣ 📜 Button.tsx
┃ ┣ 📜 Header.tsx
┃ ┗ 📜 Loading.tsx
┃
┣ 📂 api/                              # 백엔드 API
┃ ┣ 📜 axios.ts                        # Axios 공통 설정
┃ ┣ 📜 authApi.ts
┃ ┣ 📜 activityApi.ts
┃ ┣ 📜 reservationApi.ts
┃ ┣ 📜 courseApi.ts
┃ ┗ 📜 stampApi.ts
┃
┣ 📂 hooks/                            # 커스텀 Hook
┃ ┣ 📜 useWeather.ts
┃ ┣ 📜 useLocation.ts
┃ ┣ 📜 useReservation.ts
┃ ┣ 📜 useTimetable.ts
┃ ┗ 📜 useCourse.ts
┃
┣ 📂 constants/                        # 상수
┃ ┣ 📜 colors.ts
┃ ┣ 📜 routes.ts
┃ ┣ 📜 api.ts
┃ ┗ 📜 config.ts
┃
┣ 📂 types/                            # TypeScript 타입
┃ ┣ 📜 auth.ts
┃ ┣ 📜 activity.ts
┃ ┣ 📜 reservation.ts
┃ ┣ 📜 course.ts
┃ ┣ 📜 timetable.ts
┃ ┗ 📜 stamp.ts
┃
┣ 📂 utils/                            # 공통 함수
┃ ┣ 📜 date.ts
┃ ┣ 📜 format.ts
┃ ┣ 📜 location.ts
┃ ┗ 📜 validation.ts
┃
┣ 📂 assets/                           # 이미지/폰트
┃ ┣ 📂 images/
┃ ┣ 📂 icons/
┃ ┗ 📂 fonts/
┃
┣ 📂 scripts/                          # 빌드/개발용 스크립트
┃
┣ 📜 app.json
┣ 📜 package.json
┣ 📜 tsconfig.json
┗ 📜 .env                              # 환경변수
```

### 담당 도메인 요약 (프론트)

| 담당자 | 라우트 폴더 |
| --- | --- |
| 보민 | app/auth (로그인/마이페이지), app/stamp (스탬프), app/notification (알림) |
| 현진 | app/course (코스 추천 + AI) |
| 유진 | app/activity (체험 백과사전), app/reservation (예약) |
| 예빈 | app/timetable (여행 테이블), app/survey (만족도 조사) |
