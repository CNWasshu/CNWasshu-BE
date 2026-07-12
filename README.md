# CNWasshu

CNWasshu 프로젝트 백엔드 레포지토리입니다.

## Tech Stack

- Java 17
- Spring Boot 3.5.16
- MySQL 8.4
- Docker / Docker Compose

## 브랜치 전략

- `main`: 배포용
- `develop`: 개발용 (기본 작업 브랜치)

## 로컬 실행 방법

### 1. 클론

```bash
git clone https://github.com/CNWasshu/CNWasshu-BE.git
cd CNWasshu-BE/cnwasshu
git checkout develop
```

### 2. Docker 환경 변수 설정

`docker` 폴더에 `.env` 파일을 새로 생성하고 아래 내용을 채워주세요. (실제 값은 팀 슬랙/노션 또는 담당자에게 문의)

```
MYSQL_ROOT_PASSWORD=
MYSQL_DATABASE=
MYSQL_USER=
MYSQL_PASSWORD=
```

### 3. application.properties 설정

`src/main/resources` 폴더에 `application.properties` 파일을 새로 생성하고 DB 접속 정보, JWT secret 등을 채워주세요. (실제 값은 담당자에게 문의)

### 4. Docker MySQL 실행

```bash
cd docker
docker compose up -d
```

정상 실행 확인:

```bash
docker ps
```

`cnwasshu-mysql` 컨테이너가 떠 있으면 정상입니다.

### 5. 서버 실행

```bash
cd ..
./gradlew bootRun
```

서버는 `http://localhost:8080` 에서 실행됩니다.

## 주의사항

- `.env`, `application.properties` 파일은 절대 GitHub에 커밋하지 마세요. `.gitignore`에 이미 등록되어 있습니다.
- 두 파일에 필요한 값은 팀원 간 직접 공유해주세요. (Slack, 노션 등 비공개 채널 이용 권장)
