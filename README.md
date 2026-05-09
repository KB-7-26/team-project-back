# KB team-project-Backend

KB국민은행 IT's Your Life 부트캠프 팀 프로젝트  

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
            └── application.yaml         # 환경 설정 (gitignore 처리됨)
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

### 1. application.yaml 작성

`src/main/resources/application.yaml` 파일을 직접 생성합니다.  
(해당 파일은 `.gitignore`에 포함되어 있어 레포에 올라가지 않습니다.)

```yaml
spring:
  application:
    name: project-back
  datasource:
    url: jdbc:mysql://127.0.0.1:3306/kb_project
    username: root
    password: 본인비밀번호
    driver-class-name: com.mysql.cj.jdbc.Driver
  jpa:
    hibernate:
      ddl-auto: create        # 최초 실행 시 create, 이후 update로 변경
    show-sql: true
    properties:
      hibernate:
        dialect: org.hibernate.dialect.MySQLDialect

server:
  port: 8080
  servlet:
    session:
      persistent: false
```

### 2. MySQL DB 생성

```sql
CREATE DATABASE kb_project;
```

### 3. 실행

```bash
./gradlew bootRun
```

또는 IntelliJ에서 `ProjectBackApplication.java` 실행
