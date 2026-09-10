# DB 테이블 스키마 (CNWasshu)

> 이 문서는 프로젝트의 DB 테이블 설계 스펙입니다. 엔티티/마이그레이션 작성 시 이 문서를 기준으로 합니다.

## User (회원)

| 컬럼명 | 타입 | 제약조건 | 설명 |
| --- | --- | --- | --- |
| user_id | BIGINT | PK, Auto Increment | 사용자 ID |
| kakao_id | VARCHAR(50) | UNIQUE | 카카오 회원 ID |
| nickname | VARCHAR(50) | NOT NULL | 닉네임 |
| email | VARCHAR(100) |  | 이메일 |
| profile_image | VARCHAR(500) |  | 프로필 이미지 |
| onboarding_status | VARCHAR(20) | NOT NULL, DEFAULT `COMPLETED` | 온보딩 상태 (`NOT_STARTED`, `COMPLETED`, `SKIPPED`) |
| onboarding_completed_at | DATETIME |  | 온보딩 완료 또는 건너뛰기 일시 |
| created_at | DATETIME | NOT NULL | 생성일 |
| updated_at | DATETIME | NOT NULL | 수정일 |
| deleted_at | DATETIME |  | 소프트 딜리트 일시 |

애플리케이션에서 새로 가입한 사용자는 `NOT_STARTED`로 저장한다. DDL 기본값 `COMPLETED`는 기존 회원에게 온보딩이 소급 노출되지 않도록 하기 위한 값이다.

---

## UserDevice (사용자 기기 및 FCM 토큰)

멀티 디바이스 대응을 위해 User와 분리된 테이블.

| 컬럼명 | 타입 | 제약조건 | 설명 |
| --- | --- | --- | --- |
| device_id | BIGINT | PK, Auto Increment | 디바이스 ID |
| user_id | BIGINT | FK → User.user_id | 사용자 |
| fcm_token | VARCHAR(255) | NOT NULL, UNIQUE | 푸시 알림용 FCM 토큰 |
| device_type | VARCHAR(20) |  | 기기 타입 (IOS, ANDROID, WEB 등) |
| created_at | DATETIME | NOT NULL | 생성일 |
| updated_at | DATETIME | NOT NULL | 수정일 |

---

## Region (지역)

| 컬럼명 | 타입 | 제약조건 | 설명 |
| --- | --- | --- | --- |
| region_id | INT | PK | 지역 ID |
| region_name | VARCHAR(30) | NOT NULL | 지역명 (공주, 부여 등) |
| is_depopulated_area | BOOLEAN | NOT NULL | 관광 소외지역(인구 소멸 위기 지역) 여부 |
| created_at | DATETIME | NOT NULL | 생성일 |
| updated_at | DATETIME | NOT NULL | 수정일 |

---

## Category (카테고리)

| 컬럼명 | 타입 | 제약조건 | 설명 |
| --- | --- | --- | --- |
| category_id | INT | PK | 카테고리 ID |
| category_name | VARCHAR(30) | NOT NULL | 체험, 음식, 행사 등 |
| created_at | DATETIME | NOT NULL | 생성일 |
| updated_at | DATETIME | NOT NULL | 수정일 |

---

## Activity (체험 정보)

### 좌표 및 위치 검색 최적화
- 위경도는 MySQL 공간 인덱스(Spatial Index)인 `POINT` 타입 사용.
- 위치 기반 검색이 빈번하면 인덱스 필수.
- (참고) 서비스 확장 시 PostGIS 지원 PostgreSQL 전환도 고려 가능.

| 컬럼명 | 타입 | 제약조건 | 설명 |
| --- | --- | --- | --- |
| activity_id | BIGINT | PK, Auto Increment | 액티비티 ID |
| category_id | INT | FK → Category.category_id | 카테고리 |
| region_id | INT | FK → Region.region_id | 지역 |
| title | VARCHAR(100) | NOT NULL | 액티비티명 |
| short_description | VARCHAR(255) |  | 한 줄 소개 |
| description | TEXT |  | 상세 설명 |
| address | VARCHAR(255) | NOT NULL | 주소 |
| location | POINT | NOT NULL, Spatial Index | 공간 좌표 (위도/경도 통합) |
| phone | VARCHAR(30) |  | 전화번호 |
| operating_start_time | TIME |  | 운영 시작 시간 |
| operating_end_time | TIME |  | 운영 종료 시간 |
| duration | INT |  | 소요 시간(분) |
| reservation_required | BOOLEAN | NOT NULL | 예약 필요 여부 |
| today_available | BOOLEAN |  | 당일 참여 가능 여부 |
| thumbnail | VARCHAR(500) |  | 대표 이미지 |
| qr_code | VARCHAR(255) | UNIQUE | 액티비티 고유 QR 코드(또는 식별값) |
| status | ENUM |  | 운영 상태 (OPEN, CLOSED, ENDED) |
| created_at | DATETIME | NOT NULL | 등록일 |
| start_date | DATE |  | 운영 기간 (시작) |
| end_date | DATE |  | 운영 기간 (종료) |
| updated_at | DATETIME | NOT NULL | 수정일 |
| deleted_at | DATETIME |  | 소프트 딜리트 일시 |

---

## ActivityImage (액티비티 상세 페이지 이미지)

| 컬럼명 | 타입 | 제약조건 | 설명 |
| --- | --- | --- | --- |
| image_id | BIGINT | PK | 이미지 ID |
| activity_id | BIGINT | FK → Activity.activity_id | 체험 |
| image_url | VARCHAR(500) | NOT NULL | 이미지 경로 |
| sort_order | INT |  | 이미지 순서 |
| created_at | DATETIME | NOT NULL | 생성일 |
| updated_at | DATETIME | NOT NULL | 수정일 |

---

## UserBookmark (장바구니)

`UNIQUE(user_id, activity_id)` 필요.

| 컬럼명 | 타입 | 제약조건 | 설명 |
| --- | --- | --- | --- |
| bookmark_id | BIGINT | PK | 장바구니 ID |
| user_id | BIGINT | FK → User.user_id | 사용자 |
| activity_id | BIGINT | FK → Activity.activity_id | 저장한 액티비티 |
| created_at | DATETIME | NOT NULL | 저장일 |
| updated_at | DATETIME | NOT NULL | 수정일 |

---

## Reservation (예약)

| 컬럼명 | 타입 | 제약조건 | 설명 |
| --- | --- | --- | --- |
| reservation_id | BIGINT | PK | 예약 ID |
| user_id | BIGINT | FK → User.user_id | 예약자 |
| activity_id | BIGINT | FK → Activity.activity_id | 예약한 액티비티 |
| reservation_date | DATE | NOT NULL | 예약 날짜 |
| reservation_time | TIME | NOT NULL | 예약 시간 |
| status | ENUM | NOT NULL | 예약 상태 |
| created_at | DATETIME | NOT NULL | 예약일 |
| updated_at | DATETIME | NOT NULL | 수정일 |
| deleted_at | DATETIME |  | 소프트 딜리트 일시 |
| people_count | INT | NOT NULL | 여행 인원 수 |
| with_child | BOOLEAN | NOT NULL | 아이 동반 여부 |

---

## Course (여행 코스)

| 컬럼명 | 타입 | 제약조건 | 설명 |
| --- | --- | --- | --- |
| course_id | BIGINT | PK, Auto Increment | 코스 ID |
| user_id | BIGINT | FK → User.user_id | 작성자 |
| course_name | VARCHAR(100) | NOT NULL | 코스 이름 |
| course_type | ENUM | NOT NULL | USER, AI |
| people_count | INT | NOT NULL | 여행 인원 수 |
| with_child | BOOLEAN | NOT NULL | 아이 동반 여부 |
| start_date | DATE | NOT NULL | 여행 시작일 |
| end_date | DATE | NOT NULL | 여행 종료일 |
| created_at | DATETIME | NOT NULL | 생성일 |
| updated_at | DATETIME | NOT NULL | 수정일 |
| deleted_at | DATETIME |  | 소프트 딜리트 일시 |

`course_type`
- `USER` : 사용자가 직접 만든 코스
- `AI` : AI 추천을 타임테이블로 불러와 저장한 코스

---

## CourseItem (타임테이블 일정)

| 컬럼명 | 타입 | 제약조건 | 설명 |
| --- | --- | --- | --- |
| course_item_id | BIGINT | PK, Auto Increment | 일정 ID |
| course_id | BIGINT | FK → Course.course_id | 코스 ID |
| activity_id | BIGINT | FK → Activity.activity_id, NULL 허용 | 연결된 액티비티 |
| reservation_id | BIGINT | FK → Reservation.reservation_id, NULL 허용 | 연결된 예약 |
| title | VARCHAR(100) | NOT NULL | 일정 제목 |
| day_no | INT | NOT NULL | 여행 n일차 |
| start_time | TIME | NOT NULL | 시작 시간 |
| end_time | TIME | NOT NULL | 종료 시간 |
| memo | TEXT |  | 메모 |
| sort_order | INT | NOT NULL | 일정 순서 |
| created_at | DATETIME | NOT NULL | 생성일 |
| updated_at | DATETIME | NOT NULL | 수정일 |

- 직접 만든 일정 → `activity_id = NULL`, `reservation_id = NULL`
- 예약 없이 추가한 액티비티 → `activity_id`만 저장
- 예약이 필요한 액티비티 → `activity_id`, `reservation_id` 모두 저장
- AI 코스도 Course/CourseItem에 동일한 형태로 저장되며 `course_type = AI`로만 구분

---

## Stamp (스탬프)

| 컬럼명 | 타입 | 제약조건 | 설명 |
| --- | --- | --- | --- |
| stamp_id | BIGINT | PK, Auto Increment | 스탬프 ID |
| user_id | BIGINT | FK → User.user_id | 사용자 |
| activity_id | BIGINT | FK → Activity.activity_id | 인증한 액티비티 |
| photo | VARCHAR(255) | NULL | 인증 사진(스탬프 썸네일) |
| stamped_at | DATETIME | NOT NULL | 스탬프 획득 시간 |
| created_at | DATETIME | NOT NULL | 생성일 |
| updated_at | DATETIME | NOT NULL | 수정일 |

**추가 제약조건**
- `UNIQUE (user_id, activity_id)`: 동일 사용자가 동일 액티비티 스탬프 중복 획득 불가

---

## Survey (만족도 조사)

체험 종료 후 다음 날 알림이 가고, QR 찍은 것만 노출.

| 컬럼명 | 타입 | 제약조건 | 설명 |
| --- | --- | --- | --- |
| survey_id | BIGINT | PK, Auto Increment | 설문 ID |
| user_id | BIGINT | FK → User.user_id | 설문 작성자 |
| activity_id | BIGINT | FK → Activity.activity_id | 설문 대상 액티비티 |
| stamp_id | BIGINT | FK → Stamp.stamp_id | 해당 스탬프 인증 |
| is_recommended | BOOLEAN | NOT NULL | 추천 여부 (추천함 / 추천 안 함) |
| created_at | DATETIME | NOT NULL | 설문 작성일 |
| updated_at | DATETIME | NOT NULL | 수정일 |

**추가 제약조건**
- `UNIQUE(user_id, activity_id)`: 동일 사용자는 같은 액티비티에 설문 1회만 작성 가능
- 현재는 추천함/안함 2지선다로 가정

---

## NotificationSetting (알림 설정)

**흐름**: 사용자가 알림 ON/OFF 설정 → NotificationSetting 저장 → 예약 시간/코스 시작일 확인 → NotificationSetting 조회 → ON인 시간에만 FCM 푸시 발송.

코스 알림은 하루 전 고정, 예약 알림은 사용자가 시점 선택 가능.

| 컬럼명 | 타입 | 제약조건 | 설명 |
| --- | --- | --- | --- |
| notification_setting_id | BIGINT | PK, Auto Increment | 알림 설정 ID |
| user_id | BIGINT | FK → User.user_id, UNIQUE | 사용자 (1:1 관계) |
| course_day_before | BOOLEAN | NOT NULL | 코스 하루 전 알림 |
| reservation_day_before | BOOLEAN | NOT NULL | 예약 하루 전 알림 |
| reservation_3h_before | BOOLEAN | NOT NULL | 예약 3시간 전 알림 |
| reservation_1h_before | BOOLEAN | NOT NULL | 예약 1시간 전 알림 |
| reservation_30m_before | BOOLEAN | NOT NULL | 예약 30분 전 알림 |
| created_at | DATETIME | NOT NULL | 생성일 |
| updated_at | DATETIME | NOT NULL | 수정일 |

---

## Notification (알림함)

| 컬럼명 | 타입 | 제약조건 | 설명 |
| --- | --- | --- | --- |
| notification_id | BIGINT | PK, Auto Increment | 알림 ID |
| user_id | BIGINT | FK → User.user_id | 사용자 |
| reservation_id | BIGINT | FK → Reservation.reservation_id, NULL 허용 | 예약 관련 알림 |
| course_id | BIGINT | FK → Course.course_id, NULL 허용 | 코스 관련 알림 |
| activity_id | BIGINT | FK → Activity.activity_id, NULL 허용 | 설문 대상 액티비티 |
| notification_type | ENUM | NOT NULL | COURSE, RESERVATION, SURVEY |
| title | VARCHAR(100) | NOT NULL | 알림 제목 |
| content | TEXT | NOT NULL | 알림 내용 |
| is_read | BOOLEAN | DEFAULT FALSE | 읽음 여부 |
| created_at | DATETIME | NOT NULL | 알림 생성 시간 |
| updated_at | DATETIME | NOT NULL | 수정일 |

> `notification_type`에 따라 채워지는 FK가 다름: COURSE → course_id, RESERVATION → reservation_id, SURVEY → activity_id

---

## WeatherTag (날씨 태그)

| 컬럼명 | 타입 | 제약조건 | 설명 |
| --- | --- | --- | --- |
| weather_id | INT | PK | 날씨 ID |
| weather_name | VARCHAR(30) | NOT NULL | 맑음, 비, 실내 등 |
| created_at | DATETIME | NOT NULL | 생성일 |
| updated_at | DATETIME | NOT NULL | 수정일 |

---

## ActivityWeather (액티비티-날씨 연결)

| 컬럼명 | 타입 | 제약조건 | 설명 |
| --- | --- | --- | --- |
| activity_id | BIGINT | PK, FK → Activity.activity_id | 체험 |
| weather_id | INT | PK, FK → WeatherTag.weather_id | 날씨 |
| created_at | DATETIME | NOT NULL | 생성일 |
| updated_at | DATETIME | NOT NULL | 수정일 |

---

## Restaurant (맛집 정보)

| 컬럼명 | 타입 | 제약조건 | 설명 |
| --- | --- | --- | --- |
| restaurant_id | BIGINT | PK, Auto Increment | 맛집 ID |
| category_id | INT | FK → RestaurantCategory.category_id | 음식 카테고리 |
| region_id | INT | FK → Region.region_id | 지역 |
| name | VARCHAR(100) | NOT NULL | 음식점명 |
| short_description | VARCHAR(255) |  | 한 줄 소개 |
| description | TEXT |  | 상세 설명 |
| address | VARCHAR(255) | NOT NULL | 주소 |
| location | POINT | NOT NULL, Spatial Index | 위치 좌표 |
| phone | VARCHAR(30) |  | 전화번호 |
| operating_start_time | TIME |  | 영업 시작 시간 |
| operating_end_time | TIME |  | 영업 종료 시간 |
| thumbnail | VARCHAR(500) | NOT NULL | 대표 이미지 |
| status | ENUM |  | 운영 상태 (OPEN, CLOSED) |
| created_at | DATETIME | NOT NULL | 생성일 |
| updated_at | DATETIME | NOT NULL | 수정일 |
| deleted_at | DATETIME |  | 소프트 딜리트 일시 |

---

## RestaurantImage (맛집 상세 이미지)

| 컬럼명 | 타입 | 제약조건 | 설명 |
| --- | --- | --- | --- |
| image_id | BIGINT | PK, Auto Increment | 이미지 ID |
| restaurant_id | BIGINT | FK → Restaurant.restaurant_id | 맛집 |
| image_url | VARCHAR(500) | NOT NULL | 이미지 URL |
| sort_order | INT |  | 이미지 순서 |
| created_at | DATETIME | NOT NULL | 생성일 |
| updated_at | DATETIME | NOT NULL | 수정일 |
