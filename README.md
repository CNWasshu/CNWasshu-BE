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

### Gemini API 설정

AI 코스 추천을 사용하려면 서버 실행 전에 Google AI Studio에서 발급받은 키를
환경변수로 설정합니다. 키는 설정 파일이나 Git에 커밋하지 않습니다.

```bash
export GEMINI_API_KEY=발급받은_API_키
./gradlew bootRun
```

필요하면 `GEMINI_MODEL` 환경변수로 모델을 변경할 수 있으며 기본값은
`gemini-2.5-flash`입니다.

## 주의사항

- `.env`, `application.properties` 파일은 절대 GitHub에 커밋하지 마세요. `.gitignore`에 이미 등록되어 있습니다.
- 두 파일에 필요한 값은 팀원 간 직접 공유해주세요. (Slack, 노션 등 비공개 채널 이용 권장)

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
┃    ┣ 📜 CnwasshuApplication.java
┗ src/main/resources/
┣ 📜 application.yml
┗ 📜 application-local.yml

각자 하위폴더에 controller, service, repository, entity 만들면 됩니다.
