package com.example.cnwasshu.common.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    INVALID_KAKAO_LOGIN_REQUEST(HttpStatus.BAD_REQUEST, "인가코드 또는 액세스 토큰이 필요합니다."),
    KAKAO_API_ERROR(HttpStatus.BAD_GATEWAY, "카카오 API 호출에 실패했습니다."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다."),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "만료된 토큰입니다."),
    REFRESH_TOKEN_NOT_FOUND(HttpStatus.UNAUTHORIZED, "존재하지 않거나 이미 만료된 리프레시 토큰입니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."),
    INVALID_NICKNAME(HttpStatus.BAD_REQUEST, "닉네임은 공백일 수 없습니다."),

    INVALID_TIMETABLE_PERIOD(HttpStatus.BAD_REQUEST, "여행 기간이 올바르지 않습니다."),
    INVALID_TIMETABLE_DAY(HttpStatus.BAD_REQUEST, "타임테이블 날짜 구성이 올바르지 않습니다."),
    TIMETABLE_SCHEDULE_REQUIRED(HttpStatus.BAD_REQUEST, "저장할 일정을 한 개 이상 추가해 주세요."),
    INVALID_SCHEDULE_TIME(HttpStatus.BAD_REQUEST, "일정 시간이 올바르지 않습니다."),
    INVALID_SCHEDULE_TYPE(HttpStatus.BAD_REQUEST, "일정 종류와 참조 정보가 올바르지 않습니다."),
    TIMETABLE_ACTIVITY_NOT_FOUND(HttpStatus.NOT_FOUND, "체험을 찾을 수 없습니다."),
    TIMETABLE_RESTAURANT_NOT_FOUND(HttpStatus.NOT_FOUND, "음식점을 찾을 수 없습니다."),
    ACTIVITY_OUTSIDE_OPERATING_HOURS(HttpStatus.BAD_REQUEST, "체험 운영 시간 안에서 일정을 선택해 주세요."),
    RESTAURANT_OUTSIDE_OPERATING_HOURS(HttpStatus.BAD_REQUEST, "음식점 운영 시간 안에서 일정을 선택해 주세요."),
    ACTIVITY_RESERVATION_REQUIRED(HttpStatus.BAD_REQUEST, "예약 완료 후 추가할 수 있는 체험입니다."),
    INVALID_ACTIVITY_RESERVATION(HttpStatus.BAD_REQUEST, "체험과 예약 정보가 일치하지 않습니다."),
    SCHEDULE_TIME_CONFLICT(HttpStatus.CONFLICT, "같은 날짜에 시간이 겹치는 일정이 있습니다."),

    RESERVATION_TIME_CONFLICT(HttpStatus.CONFLICT, "해당 시간에 이미 예약된 체험이 있습니다."),
    RESERVATION_NOT_FOUND(HttpStatus.NOT_FOUND, "예약 내역을 찾을 수 없습니다."),
    RESERVATION_ALREADY_CANCELLED(HttpStatus.CONFLICT, "이미 취소된 예약입니다."),
    RESERVATION_CANCEL_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "당일 예약은 앱에서 취소할 수 없습니다. 전화로 문의해 주세요."),

    NOTIFICATION_NOT_FOUND(HttpStatus.NOT_FOUND, "알림을 찾을 수 없습니다."),

    SURVEY_NOT_FOUND(HttpStatus.NOT_FOUND, "만족도 조사를 찾을 수 없습니다."),
    ACTIVITY_SURVEY_NOT_FOUND(HttpStatus.NOT_FOUND, "체험 만족도 조사 항목을 찾을 수 없습니다."),
    SURVEY_NOT_EDITABLE(HttpStatus.CONFLICT, "수정할 수 없는 상태의 만족도 조사입니다."),
    INVALID_SURVEY_RESPONSE(HttpStatus.BAD_REQUEST, "필수 만족도 조사 항목을 확인해 주세요."),

    STAMP_ACTIVITY_NOT_FOUND(HttpStatus.NOT_FOUND, "QR 코드에 해당하는 체험을 찾을 수 없습니다."),
    STAMP_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 획득한 스탬프입니다.");

    private final HttpStatus status;
    private final String message;
}
