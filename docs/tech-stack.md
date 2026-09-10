# 기술 스택 & 초기 세팅 (CNWasshu)

## 스택 개요

- **프론트**: React (Expo)
- **백엔드**: Spring Boot / OpenJDK 17.0.18 LTS
- **DB**: MySQL 8.4 (로컬 + Docker)
- **파일 저장**: 사진/동영상 - 로컬(개발), 추후 S3

## 버전 통일 기준

| 항목 | 버전 |
| --- | --- |
| Java | 17 LTS |
| Spring Boot | 3.5.4 |
| Gradle | 8.14.3 (Wrapper 사용) |
| Node.js | 22.18.0 LTS |
| npm | 10.9.x |
| Expo | SDK 54 |
| React | 19.x |
| Docker Desktop | 4.46.x 이상 |
| MySQL | 8.4 LTS |
| Git | 2.51.x 이상 |
| IntelliJ IDEA Community | 2025.2.2 이상 |
| VS Code | 최신 Stable |

## 프로젝트 구조

```
TravelActivity/
├── backend        ← Spring Boot
├── frontend       ← React + Expo(Web)
└── docker         ← docker-compose.yml
```
backend 안에 frontend를 만들지 않는다 (완전 분리).

## 백엔드 build.gradle 필수 의존성

```
dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-web'
    implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
    runtimeOnly 'com.mysql:mysql-connector-j'
}
```

## Docker (MySQL) 세팅

`docker/docker-compose.yml`:

```yaml
services:
  mysql:
    image: mysql:8.4
    container_name: cnwasshu-mysql
    restart: always
    environment:
      MYSQL_ROOT_PASSWORD: 1234
      MYSQL_DATABASE: cnwasshu
      MYSQL_USER: cnwasshu
      MYSQL_PASSWORD: 1234
    ports:
      - "3306:3306"
    volumes:
      - mysql_data:/var/lib/mysql

volumes:
  mysql_data:
```

실행: `docker compose up -d` (docker 폴더에서)
확인: `docker ps` → `cnwasshu-mysql` STATUS `Up`

## application.yml (로컬 개발 기준)

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/cnwasshu
    username: cnwasshu
    password: 1234
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
    properties:
      hibernate:
        format_sql: true
```

> ⚠️ 환경 변수 원칙: DB 비밀번호, API Key 등 민감정보는 절대 코드/설정파일에 하드코딩하지 않고 `.env` 또는 `System.getenv(...)`로 관리. 배포 시 값만 교체하면 되도록 구성.

## 프론트 초기 세팅

```
npx create-expo-app@latest frontend
```

필수 라이브러리:
```
npm install axios
npm install react-router-dom
npm install zustand
npm install @tanstack/react-query
npm install react-hook-form
npm install dayjs
```

UI:
```
npm install @mui/material
npm install @emotion/react
npm install @emotion/styled
```

실행:
```
npx expo start --web
# http://localhost:8081
```

## 매일 개발 시작 순서

1. Docker Desktop 실행
2. `docker compose up -d` (docker 폴더, 컨테이너 꺼져있으면)
3. Spring Boot 실행 (`./gradlew bootRun` 또는 IntelliJ Run)
4. `cd frontend`
5. `npx expo start --web`

## CORS

React(`http://localhost:8081`) → Spring Boot(`http://localhost:8080`) API 호출 예정이므로, 백엔드 초기 세팅 시 CORS 설정 필수.

## 배포 아키텍처 (참고, 심사 2~3주 전부터 적용)

| 구성요소 | 서비스 |
| --- | --- |
| 프론트엔드 | Vercel (main 브랜치 push 시 자동 배포) |
| 백엔드 | AWS EC2 (Docker 기반, GitHub Actions로 CI/CD) |
| DB | AWS RDS (MySQL) |
| 파일 스토리지 | AWS S3 |

### 배포 도입 타이밍
- **~개발 70% 시점**: AWS 없이 로컬 Docker 개발 + 프론트는 Vercel에 무료 연동해 팀 공유
- **심사 2~3주 전**: AWS 프리티어 계정 생성, EC2에 Docker 설치 후 배포 테스트
- **심사 1주일 전**: RDS/S3 연결 및 최종 점검

### 배포 체크리스트
- [ ] Vercel 연동 (`npx expo export` 빌드 결과물 배포)
- [ ] AWS 프리티어 인스턴스(t3.micro) 생성
- [ ] 로컬 `docker-compose.yml`을 EC2에서도 동일하게 사용
- [ ] `.github/workflows/deploy.yml` 작성 (자동 배포 파이프라인)
- [ ] 도메인 연결 (예: `chungnam-exp.vercel.app`)

Docker를 쓰는 이유: 로컬 개발 환경과 배포 환경이 동일해져서 "내 컴퓨터에선 되는데 서버에선 안 되는" 문제가 사라짐.
