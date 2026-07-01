# KB team-project-Backend

KB국민은행 IT's Your Life 부트캠프 팀 프로젝트!  

---

## 폴더 구조

```
project-back/
└── src/
    └── main/
        ├── java/com/example/projectback/
        │   ├── ProjectBackApplication.java
        │   ├── entity/                  # JPA Entity 클래스 (공통)
        │   │   ├── User.java
        │   │   ├── Category.java
        │   │   ├── Product.java
        │   │   ├── ProductMethod.java
        │   │   ├── ProductImage.java
        │   │   ├── ProductFavorite.java
        │   │   ├── PointLog.java
        │   │   ├── ChatRoom.java
        │   │   ├── ChatMessage.java
        │   │   ├── Transaction.java
        │   │   ├── Review.java
        │   │   ├── BoardPost.java
        │   │   └── BoardComment.java
        │   ├── user/                    # 회원/인증 (담당: 민철)
        │   │   ├── controller/
        │   │   ├── service/
        │   │   ├── repository/
        │   │   └── dto/
        │   ├── product/                 # 상품 (담당: 지민)
        │   │   ├── controller/
        │   │   ├── service/
        │   │   ├── repository/
        │   │   └── dto/
        │   ├── board/                   # 게시판 (담당: 기선)
        │   │   ├── controller/
        │   │   ├── service/
        │   │   ├── repository/
        │   │   └── dto/
        │   ├── chat/                    # 채팅 (담당: 대주)
        │   │   ├── controller/
        │   │   ├── service/
        │   │   ├── repository/
        │   │   └── dto/
        │   └── config/                  # 공통 설정
        │       ├── SecurityConfig.java
        │       └── SwaggerConfig.java
        └── resources/
            └── application.yaml         # 환경변수 기반 공통 설정
```

---

## DB 테이블 구조

총 13개 테이블로 구성됩니다.

| 테이블 | 설명 |
|--------|------|
| users | 회원 정보 |
| categories | 상품 카테고리 (자기참조, 계층 구조) |
| products | 상품 정보 |
| product_methods | 거래 방법 (직거래/택배, 1:N) |
| product_images | 상품 이미지 (1:N) |
| product_favorites | 상품 찜 |
| point_logs | 포인트/신뢰도 변동 내역 |
| chat_rooms | 채팅방 |
| chat_messages | 채팅 메시지 |
| transactions | 거래 내역 |
| reviews | 거래 후기 |
| board_posts | 게시글 |
| board_comments | 댓글 / 대댓글 (자기참조) |

---

## 로컬 실행 방법

### 1. 환경변수 파일 작성

```bash
cp .env.example .env
```

`.env`는 로컬 전용 파일이며 `.gitignore`에 포함되어 레포에 올라가지 않습니다.
Firebase Admin SDK 서비스 계정 JSON은 백엔드 repo 밖의 `../secrets/firebase-service-account.json`에 두는 구성을 기본값으로 사용합니다.

### 2. MySQL DB 생성

```sql
CREATE DATABASE kb_project;
```

### 3. 실행

```bash
./gradlew bootRun
```

또는 IntelliJ에서 `ProjectBackApplication.java` 실행

## 배포 환경변수

GitHub Secrets 또는 배포 플랫폼 환경변수에는 `.env.example`의 키 이름을 그대로 등록합니다.

| 변수 | 설명 |
|------|------|
| `SERVER_PORT` | Spring Boot 실행 포트 |
| `SPRING_DATASOURCE_URL` | 운영 DB JDBC URL |
| `SPRING_DATASOURCE_USERNAME` | 운영 DB 사용자명 |
| `SPRING_DATASOURCE_PASSWORD` | 운영 DB 비밀번호 |
| `SPRING_JPA_HIBERNATE_DDL_AUTO` | 운영에서는 보통 `validate` 또는 `none` 권장 |
| `SPRING_JPA_SHOW_SQL` | 운영에서는 보통 `false` 권장 |
| `FIREBASE_SERVICE_ACCOUNT_JSON` | Firebase Admin SDK 서비스 계정 JSON. raw JSON 한 줄 또는 base64 문자열 사용 가능 |
| `STORAGE_TYPE` | 로컬은 `local`, 배포는 `s3` |
| `IMAGE_UPLOAD_DIR` | `STORAGE_TYPE=local`일 때 업로드 파일 저장 경로 |
| `IMAGE_BASE_URL` | `STORAGE_TYPE=local`일 때 업로드 파일 public base URL |
| `AWS_REGION` | `STORAGE_TYPE=s3`일 때 AWS 리전 |
| `S3_BUCKET_NAME` | `STORAGE_TYPE=s3`일 때 업로드 파일을 저장할 S3 bucket |
| `S3_OBJECT_PREFIX` | `STORAGE_TYPE=s3`일 때 object key prefix |
| `S3_PUBLIC_BASE_URL` | `STORAGE_TYPE=s3`일 때 CloudFront 또는 S3 public base URL |
| `APP_CORS_ALLOWED_ORIGIN_PATTERNS` | 프론트 배포 도메인. 여러 개면 comma로 구분 |
