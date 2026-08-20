# 타임테이블 API 명세

## 1. 범위와 저장 모델

타임테이블 API는 사용자가 여행 기간과 날짜별 일정을 구성하고 저장·조회·수정하는 기능을 담당한다.

- API 경로는 `/api/timetables`를 사용한다.
- 인증 사용자는 `CustomUserPrincipal`에서 식별한다.
- 별도 타임테이블 테이블을 만들지 않고 기존 `Course`, `CourseItem` 엔티티를 사용한다.
- 타임테이블에서 직접 저장한 코스는 `CourseType.USER`로 저장한다.
- API의 `timetableId`는 DB의 `course_id`와 같다.
- 작성 중인 타임테이블은 프론트엔드 로컬 상태로 관리하고, 저장 버튼을 누를 때 전체 데이터를 서버로 전송한다.

### 일정 데이터 구분

| 일정 종류 | activityId | reservationId |
| --- | --- | --- |
| 자유 일정 | `null` | `null` |
| 예약 불필요 체험 | 필수 | `null` |
| 예약 완료 체험 | 필수 | 필수 |

예약 필수 체험은 체험 선택 직후 저장하지 않는다. 예약 페이지에서 예약을 완료한 뒤 발급받은 `reservationId`와 함께 타임테이블에 추가한다. 예약을 취소하거나 예약에 실패하면 타임테이블에 추가하지 않는다.

## 2. 공통 규칙

### 날짜와 시간

- 날짜: `YYYY-MM-DD`
- 시간: `HH:mm`
- 기준 시간대: `Asia/Seoul`
- 여행 기간: 시작일과 종료일을 포함해 최대 7일
- 자정을 넘는 일정은 허용하지 않으며 다음 날짜 일정으로 분리한다.

### 운영시간

- `HOURS`: 일정 시간이 체험 운영 시작·종료 시간 안에 있어야 한다.
- `ALWAYS`: 자유 일정과 동일하게 하루 안에서 시간을 선택할 수 있다.
- 상시 운영 체험이나 자유 일정이 기본 표시 범위인 `09:00~22:00`을 벗어나면 프론트 타임라인이 일정 시간에 맞게 확장된다.

### 공통 검증

- 종료 시간은 시작 시간보다 늦어야 한다.
- 같은 날짜의 일정은 서로 겹치면 안 된다.
- 일정의 `dayNo`는 여행 기간 안에 있어야 한다.
- `sortOrder`는 같은 날짜 안의 표시 순서다.
- 요청 사용자가 소유한 타임테이블만 조회·수정할 수 있다.
- `CourseType.USER`가 아닌 코스는 타임테이블 API로 수정하지 않는다.

## 3. 타임테이블 생성

```http
POST /api/timetables
```

### 요청

```json
{
  "timetableName": "충남 2박 3일 여행",
  "peopleCount": 2,
  "withChild": false,
  "startDate": "2026-08-20",
  "endDate": "2026-08-22",
  "days": [
    {
      "dayNo": 1,
      "date": "2026-08-20",
      "schedules": [
        {
          "clientScheduleId": "local-1",
          "scheduleType": "FREE",
          "activityId": null,
          "reservationId": null,
          "title": "점심 식사",
          "startTime": "12:00",
          "endTime": "13:00",
          "memo": null,
          "sortOrder": 1
        },
        {
          "clientScheduleId": "local-2",
          "scheduleType": "ACTIVITY",
          "activityId": 101,
          "reservationId": 9001,
          "title": "논산 딸기 수확 체험",
          "startTime": "15:00",
          "endTime": "17:00",
          "memo": null,
          "sortOrder": 2
        }
      ]
    }
  ]
}
```

`clientScheduleId`는 저장 실패 시 충돌한 프론트 일정 항목을 식별하는 용도로 사용하며 DB에는 저장하지 않는다.

### 성공 응답

```http
201 Created
```

```json
{
  "timetableId": 31,
  "timetableName": "충남 2박 3일 여행",
  "peopleCount": 2,
  "withChild": false,
  "startDate": "2026-08-20",
  "endDate": "2026-08-22",
  "days": [
    {
      "dayNo": 1,
      "date": "2026-08-20",
      "schedules": [
        {
          "scheduleId": 501,
          "scheduleType": "FREE",
          "activityId": null,
          "reservationId": null,
          "title": "점심 식사",
          "startTime": "12:00",
          "endTime": "13:00",
          "memo": null,
          "sortOrder": 1
        }
      ]
    }
  ]
}
```

## 4. 내 타임테이블 목록 조회

```http
GET /api/timetables
```

### 성공 응답

```http
200 OK
```

```json
{
  "items": [
    {
      "timetableId": 31,
      "timetableName": "충남 2박 3일 여행",
      "startDate": "2026-08-20",
      "endDate": "2026-08-22",
      "scheduleCount": 7,
      "createdAt": "2026-08-20T15:30:00",
      "updatedAt": "2026-08-20T16:10:00"
    }
  ]
}
```

초기 구현은 전체 목록을 최신 생성 순으로 반환한다. 페이지네이션은 저장 개수 정책이 확정되면 추가한다.

## 5. 타임테이블 상세 조회

```http
GET /api/timetables/{timetableId}
```

- 저장된 타임테이블을 다시 열거나 수정할 때 사용한다.
- 생성 API의 성공 응답과 같은 상세 구조를 반환한다.
- 일정은 `dayNo`, `sortOrder`, `startTime` 순으로 정렬한다.

## 6. 타임테이블 전체 수정

```http
PUT /api/timetables/{timetableId}
```

- 타임테이블 기본 정보와 전체 일정 목록을 한 번에 교체한다.
- 요청 구조는 생성 요청과 같다.
- 기존 `CourseItem`은 요청에 포함된 일정 목록으로 교체한다.
- 생성 API와 동일한 여행 기간, 운영시간, 예약 및 시간 중복 검증을 수행한다.
- 성공 시 수정된 상세 정보를 `200 OK`로 반환한다.

## 7. 담아둔 체험 조회

```http
GET /api/timetables/saved-activities
```

프론트엔드의 임시 `SAVED_ACTIVITIES`를 대체하며, 사용자가 북마크한 체험을 타임테이블 일정 추가에 필요한 형태로 반환한다.

```json
{
  "items": [
    {
      "activityId": 101,
      "title": "논산 딸기 수확 체험",
      "thumbnailUrl": "https://example.com/activities/101.jpg",
      "region": "논산",
      "operatingType": "HOURS",
      "operatingStartTime": "10:00",
      "operatingEndTime": "17:00",
      "durationMinutes": 120,
      "reservationRequired": true
    }
  ]
}
```

`operatingType`은 운영 시작·종료 시간이 모두 없으면 `ALWAYS`, 모두 있으면 `HOURS`로 표현한다. 한쪽 시간만 존재하는 잘못된 체험 데이터는 반환하지 않고 서버 데이터 오류로 처리한다.

## 8. 예약 완료 체험 추가 규칙

예약 필수 체험을 선택하면 예약 페이지로 이동한다. 예약 API가 성공한 경우 프론트엔드는 아래 값을 일정에 포함한다.

```json
{
  "scheduleType": "ACTIVITY",
  "activityId": 101,
  "reservationId": 9001,
  "title": "논산 딸기 수확 체험",
  "startTime": "10:00",
  "endTime": "12:00"
}
```

서버는 다음을 검증한다.

- 예약이 현재 사용자의 것인지
- 예약의 `activityId`가 일정의 `activityId`와 일치하는지
- 예약 날짜가 해당 `dayNo`의 실제 날짜와 일치하는지
- 예약 상태가 확정 상태인지
- 시작 시간이 예약 시간과 일치하는지
- 종료 시간이 `예약 시간 + 체험 소요 시간`과 일치하는지

## 9. 오류 응답

공통 오류 형식을 사용한다.

```json
{
  "code": "SCHEDULE_TIME_CONFLICT",
  "message": "같은 날짜에 시간이 겹치는 일정이 있습니다."
}
```

| HTTP 상태 | 코드 | 의미 |
| --- | --- | --- |
| 400 | `INVALID_TIMETABLE_PERIOD` | 여행 기간이 올바르지 않음 |
| 400 | `INVALID_SCHEDULE_TIME` | 시작·종료 시간이 올바르지 않음 |
| 400 | `INVALID_SCHEDULE_TYPE` | 일정 종류와 참조 ID 조합이 올바르지 않음 |
| 400 | `ACTIVITY_OUTSIDE_OPERATING_HOURS` | 체험 운영시간을 벗어남 |
| 400 | `ACTIVITY_RESERVATION_REQUIRED` | 예약 필수 체험에 완료된 예약이 없음 |
| 400 | `INVALID_ACTIVITY_RESERVATION` | 예약의 사용자·체험·날짜·시간이 일치하지 않음 |
| 409 | `SCHEDULE_TIME_CONFLICT` | 같은 날짜의 일정 시간이 겹침 |
| 404 | `TIMETABLE_NOT_FOUND` | 타임테이블이 없거나 현재 사용자가 소유하지 않음 |

소유자가 아닌 경우에도 다른 사용자의 타임테이블 존재 여부를 노출하지 않도록 `404 Not Found`를 반환한다.
