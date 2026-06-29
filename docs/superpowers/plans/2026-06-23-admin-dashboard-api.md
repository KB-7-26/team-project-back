# Admin Dashboard API Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 관리자 대시보드용 신규 API 5개 추가 — 기수별 신고 현황, 유저 신고 요약, 기수 목록, 기수 필터, 활동 통계

**Architecture:** 기존 AdminController/AdminService 패턴을 그대로 확장한다. DTO 4개를 신규 생성하고, 각 Repository에 집계 쿼리 메서드를 추가한 뒤, Service → Controller 순으로 구현한다. 테스트는 AdminServiceTest에 Mockito 기반 단위 테스트를 추가한다.

**Tech Stack:** Spring Boot 3, Spring Data JPA, Mockito + JUnit 5, Lombok

## Global Constraints

- 응답은 `ApiResponse.success(data, "메시지")` 래퍼 필수 — `{ success, data, message }`
- 인증: `@PreAuthorize("hasRole('ADMIN')")` (클래스 레벨, 기존 설정 유지)
- 기본 URL prefix: `/api/admin`
- DTO는 `@Getter` + private 생성자 + 정적 팩토리 패턴 (기존 AdminUserResponse 참고)
- 테스트는 `@ExtendWith(MockitoExtension.class)` + `given/willReturn` + `assertThat`
- 신고 카테고리 정의:
  - `product` = `UserReport` WHERE `product.seller.id = userId` (상품 판매자로서 신고됨)
  - `post` = `BoardPostReport` WHERE `post.author.id = userId`
  - `comment` = `BoardCommentReport` WHERE `comment.author.id = userId`
  - `user` = `UserReport` WHERE `reportedUser.id = userId` (직접 유저 신고)

---

### Task 1: DTO 4개 생성

**Files:**
- Create: `src/main/java/com/example/projectback/admin/dto/ReportsByCohortResponse.java`
- Create: `src/main/java/com/example/projectback/admin/dto/UserReportSummaryResponse.java`
- Create: `src/main/java/com/example/projectback/admin/dto/CohortSummaryResponse.java`
- Create: `src/main/java/com/example/projectback/admin/dto/UserActivitySummaryResponse.java`

**Interfaces:**
- Produces: 4개의 응답 DTO — 이후 Task의 서비스/테스트가 이 타입을 사용

- [ ] **Step 1: ReportsByCohortResponse 생성**

```java
// src/main/java/com/example/projectback/admin/dto/ReportsByCohortResponse.java
package com.example.projectback.admin.dto;

import com.example.projectback.entity.User;
import lombok.Getter;

import java.util.List;

@Getter
public class ReportsByCohortResponse {

    private final String cohort;
    private final int reportedUserCount;
    private final int totalReportCount;
    private final List<ReportedUserSummary> users;

    public ReportsByCohortResponse(String cohort, int reportedUserCount, int totalReportCount, List<ReportedUserSummary> users) {
        this.cohort = cohort;
        this.reportedUserCount = reportedUserCount;
        this.totalReportCount = totalReportCount;
        this.users = users;
    }

    @Getter
    public static class ReportedUserSummary {
        private final long userId;
        private final String nickname;
        private final String email;
        private final int totalReportCount;

        public ReportedUserSummary(User user, int totalReportCount) {
            this.userId = user.getId();
            this.nickname = user.getNickname();
            this.email = user.getEmail();
            this.totalReportCount = totalReportCount;
        }
    }
}
```

- [ ] **Step 2: UserReportSummaryResponse 생성**

```java
// src/main/java/com/example/projectback/admin/dto/UserReportSummaryResponse.java
package com.example.projectback.admin.dto;

import lombok.Getter;

@Getter
public class UserReportSummaryResponse {

    private final int product;
    private final int post;
    private final int comment;
    private final int user;

    public UserReportSummaryResponse(int product, int post, int comment, int user) {
        this.product = product;
        this.post = post;
        this.comment = comment;
        this.user = user;
    }
}
```

- [ ] **Step 3: CohortSummaryResponse 생성**

```java
// src/main/java/com/example/projectback/admin/dto/CohortSummaryResponse.java
package com.example.projectback.admin.dto;

import lombok.Getter;

@Getter
public class CohortSummaryResponse {

    private final String cohort;
    private final long userCount;

    public CohortSummaryResponse(String cohort, Long userCount) {
        this.cohort = cohort;
        this.userCount = userCount;
    }
}
```

- [ ] **Step 4: UserActivitySummaryResponse 생성**

```java
// src/main/java/com/example/projectback/admin/dto/UserActivitySummaryResponse.java
package com.example.projectback.admin.dto;

import lombok.Getter;

@Getter
public class UserActivitySummaryResponse {

    private final long productCount;
    private final long salesCount;
    private final long purchaseCount;
    private final long postCount;
    private final long commentCount;

    public UserActivitySummaryResponse(long productCount, long salesCount, long purchaseCount, long postCount, long commentCount) {
        this.productCount = productCount;
        this.salesCount = salesCount;
        this.purchaseCount = purchaseCount;
        this.postCount = postCount;
        this.commentCount = commentCount;
    }
}
```

- [ ] **Step 5: 컴파일 확인**

```bash
./gradlew compileJava
```
Expected: BUILD SUCCESSFUL

- [ ] **Step 6: Commit**

```bash
git add src/main/java/com/example/projectback/admin/dto/ReportsByCohortResponse.java \
        src/main/java/com/example/projectback/admin/dto/UserReportSummaryResponse.java \
        src/main/java/com/example/projectback/admin/dto/CohortSummaryResponse.java \
        src/main/java/com/example/projectback/admin/dto/UserActivitySummaryResponse.java
git commit -m "feat: [admin] 관리자 대시보드 신규 API 응답 DTO 추가"
```

---

### Task 2: Repository 집계 쿼리 메서드 추가

**Files:**
- Modify: `src/main/java/com/example/projectback/user/repository/UserRepository.java`
- Modify: `src/main/java/com/example/projectback/report/repository/UserReportRepository.java`
- Modify: `src/main/java/com/example/projectback/board/repository/BoardPostReportRepository.java`
- Modify: `src/main/java/com/example/projectback/board/repository/BoardCommentReportRepository.java`
- Modify: `src/main/java/com/example/projectback/transaction/repository/TransactionRepository.java`
- Modify: `src/main/java/com/example/projectback/product/repository/ProductRepository.java`

**Interfaces:**
- Consumes: 기존 Repository 인터페이스들
- Produces: 서비스에서 호출할 집계 메서드들

- [ ] **Step 1: UserRepository에 cohort 관련 메서드 추가**

기존 파일에 다음 메서드를 추가한다:

```java
import com.example.projectback.admin.dto.CohortSummaryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;

// 기존 메서드들 아래에 추가:

Page<User> findByCohort(String cohort, Pageable pageable);

@Query("SELECT new com.example.projectback.admin.dto.CohortSummaryResponse(u.cohort, COUNT(u)) " +
       "FROM User u GROUP BY u.cohort ORDER BY u.cohort DESC")
List<CohortSummaryResponse> findCohortSummary();
```

완성된 UserRepository.java:

```java
package com.example.projectback.user.repository;

import com.example.projectback.admin.dto.CohortSummaryResponse;
import com.example.projectback.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByFirebaseUid(String firebaseUid);

    Optional<User> findByEmail(String email);

    boolean existsByFirebaseUid(String firebaseUid);

    boolean existsByEmail(String email);

    boolean existsByNickname(String nickname);

    boolean existsByNicknameAndIdNot(String nickname, Long id);

    Page<User> findByCohort(String cohort, Pageable pageable);

    @Query("SELECT new com.example.projectback.admin.dto.CohortSummaryResponse(u.cohort, COUNT(u)) " +
           "FROM User u GROUP BY u.cohort ORDER BY u.cohort DESC")
    List<CohortSummaryResponse> findCohortSummary();
}
```

- [ ] **Step 2: UserReportRepository에 집계 메서드 추가**

완성된 UserReportRepository.java:

```java
package com.example.projectback.report.repository;

import com.example.projectback.entity.UserReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserReportRepository extends JpaRepository<UserReport, Long> {

    boolean existsByReporterIdAndReportedUserIdAndProductId(Long reporterId, Long reportedUserId, Long productId);

    void deleteByProductId(Long productId);

    void deleteByReporterId(Long reporterId);

    void deleteByReportedUserId(Long reportedUserId);

    List<UserReport> findAllByOrderByCreatedAtDesc();

    long countByReportedUserId(Long reportedUserId);

    @Query("SELECT COUNT(r) FROM UserReport r WHERE r.product.seller.id = :userId")
    long countByProductSellerId(@Param("userId") Long userId);

    @Query("SELECT DISTINCT r.reportedUser.id FROM UserReport r")
    List<Long> findDistinctReportedUserIds();
}
```

- [ ] **Step 3: BoardPostReportRepository에 집계 메서드 추가**

완성된 BoardPostReportRepository.java:

```java
package com.example.projectback.board.repository;

import com.example.projectback.entity.BoardPostReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BoardPostReportRepository extends JpaRepository<BoardPostReport, Long> {

    boolean existsByUserIdAndPostId(Long userId, Long postId);

    void deleteByPostId(Long postId);

    void deleteByUserId(Long userId);

    @Query("SELECT COUNT(r) FROM BoardPostReport r WHERE r.post.author.id = :userId")
    long countByPostAuthorId(@Param("userId") Long userId);

    @Query("SELECT DISTINCT r.post.author.id FROM BoardPostReport r")
    List<Long> findDistinctReportedAuthorIds();
}
```

- [ ] **Step 4: BoardCommentReportRepository에 집계 메서드 추가**

완성된 BoardCommentReportRepository.java:

```java
package com.example.projectback.board.repository;

import com.example.projectback.entity.BoardCommentReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface BoardCommentReportRepository extends JpaRepository<BoardCommentReport, Long> {

    boolean existsByUserIdAndCommentId(Long userId, Long commentId);

    void deleteByUserId(Long userId);

    void deleteByCommentIdIn(Collection<Long> commentIds);

    @Query("SELECT COUNT(r) FROM BoardCommentReport r WHERE r.comment.author.id = :userId")
    long countByCommentAuthorId(@Param("userId") Long userId);

    @Query("SELECT DISTINCT r.comment.author.id FROM BoardCommentReport r")
    List<Long> findDistinctReportedAuthorIds();
}
```

- [ ] **Step 5: TransactionRepository에 집계 메서드 추가**

기존 TransactionRepository.java에 다음 추가:

```java
long countBySellerIdAndStatus(Long sellerId, String status);

long countByBuyerIdAndStatus(Long buyerId, String status);
```

완성된 TransactionRepository.java (끝 부분):

```java
    // 채팅방으로 거래 조회
    java.util.Optional<Transaction> findByChatRoomId(Long chatRoomId);

    long countBySellerIdAndStatus(Long sellerId, String status);

    long countByBuyerIdAndStatus(Long buyerId, String status);
}
```

- [ ] **Step 6: ProductRepository에 집계 메서드 추가**

기존 `ProductRepository.java`에 다음 추가 (이미 있는 `countBySellerIdAndSaleStatus` 아래에):

```java
long countBySellerId(Long sellerId);
```

- [ ] **Step 7: 컴파일 확인**

```bash
./gradlew compileJava
```
Expected: BUILD SUCCESSFUL

- [ ] **Step 8: Commit**

```bash
git add src/main/java/com/example/projectback/user/repository/UserRepository.java \
        src/main/java/com/example/projectback/report/repository/UserReportRepository.java \
        src/main/java/com/example/projectback/board/repository/BoardPostReportRepository.java \
        src/main/java/com/example/projectback/board/repository/BoardCommentReportRepository.java \
        src/main/java/com/example/projectback/transaction/repository/TransactionRepository.java \
        src/main/java/com/example/projectback/product/repository/ProductRepository.java
git commit -m "feat: [admin] 관리자 대시보드용 Repository 집계 쿼리 추가"
```

---

### Task 3: AdminService 신규 메서드 테스트 작성 (실패 확인)

**Files:**
- Modify: `src/test/java/com/example/projectback/admin/service/AdminServiceTest.java`

**Interfaces:**
- Consumes: Task 1의 DTO 타입들, Task 2의 repository 메서드들
- Produces: AdminService의 신규 메서드 5개에 대한 실패 테스트

- [ ] **Step 1: AdminServiceTest에 새 Mock/테스트 추가**

기존 파일을 열고 아래 내용으로 교체한다:

```java
package com.example.projectback.admin.service;

import com.example.projectback.admin.dto.*;
import com.example.projectback.board.repository.*;
import com.example.projectback.board.service.BoardPostService;
import com.example.projectback.entity.User;
import com.example.projectback.entity.UserRole;
import com.example.projectback.product.repository.ProductFavoriteRepository;
import com.example.projectback.product.repository.ProductRepository;
import com.example.projectback.product.service.ProductService;
import com.example.projectback.report.repository.UserReportRepository;
import com.example.projectback.transaction.repository.TransactionRepository;
import com.example.projectback.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

    @Mock UserRepository userRepository;
    @Mock UserReportRepository userReportRepository;
    @Mock BoardPostRepository boardPostRepository;
    @Mock BoardCommentRepository boardCommentRepository;
    @Mock BoardCommentLikeRepository boardCommentLikeRepository;
    @Mock BoardCommentReportRepository boardCommentReportRepository;
    @Mock BoardPostLikeRepository boardPostLikeRepository;
    @Mock BoardPostReportRepository boardPostReportRepository;
    @Mock ProductRepository productRepository;
    @Mock ProductFavoriteRepository productFavoriteRepository;
    @Mock BoardPostService boardPostService;
    @Mock ProductService productService;
    @Mock TransactionRepository transactionRepository;

    @InjectMocks
    AdminService adminService;

    private User normalUser;

    @BeforeEach
    void setUp() {
        normalUser = User.builder()
                .firebaseUid("uid-1")
                .name("홍길동")
                .nickname("길동")
                .email("user@example.com")
                .gender("M")
                .cohort("30기")
                .build();
    }

    // ─────────────── 기존 테스트 ───────────────

    @Test
    @DisplayName("toggleSuspend - 정지되지 않은 유저를 정지시킨다")
    void toggleSuspend_suspends_unsuspended_user() {
        given(userRepository.findById(1L)).willReturn(Optional.of(normalUser));

        AdminUserResponse response = adminService.toggleSuspend(1L);

        assertThat(response.getIsSuspended()).isTrue();
    }

    @Test
    @DisplayName("toggleSuspend - 이미 정지된 유저를 해제한다")
    void toggleSuspend_unsuspends_suspended_user() {
        normalUser.suspend();
        given(userRepository.findById(1L)).willReturn(Optional.of(normalUser));

        AdminUserResponse response = adminService.toggleSuspend(1L);

        assertThat(response.getIsSuspended()).isFalse();
    }

    @Test
    @DisplayName("toggleSuspend - 존재하지 않는 유저면 예외 발생")
    void toggleSuspend_throws_when_user_not_found() {
        given(userRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> adminService.toggleSuspend(999L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("유저를 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("forceWithdraw - 유저 및 연관 데이터 삭제 호출 검증")
    void forceWithdraw_deletes_user_and_related_data() {
        given(userRepository.findById(1L)).willReturn(Optional.of(normalUser));
        given(boardCommentRepository.findIdsByAuthorId(1L)).willReturn(List.of());
        given(boardCommentRepository.findReplyIdsByParentAuthorId(1L)).willReturn(List.of());
        given(boardPostRepository.findIdsByAuthorId(1L)).willReturn(List.of());
        given(productRepository.findIdsBySellerId(1L)).willReturn(List.of());

        adminService.forceWithdraw(1L);

        verify(boardPostLikeRepository).deleteByUserId(1L);
        verify(boardPostReportRepository).deleteByUserId(1L);
        verify(productFavoriteRepository).deleteByUserId(1L);
        verify(userReportRepository).deleteByReporterId(1L);
        verify(userReportRepository).deleteByReportedUserId(1L);
        verify(userRepository).delete(normalUser);
    }

    @Test
    @DisplayName("User 신규 생성 시 role은 기본값 USER")
    void newUser_hasDefaultRoleUser() {
        assertThat(normalUser.getRole()).isEqualTo(UserRole.USER);
    }

    @Test
    @DisplayName("User 신규 생성 시 isSuspended는 기본값 false")
    void newUser_isNotSuspendedByDefault() {
        assertThat(normalUser.getIsSuspended()).isFalse();
    }

    // ─────────────── 신규 테스트 ───────────────

    @Test
    @DisplayName("getUserReportSummary - 카테고리별 신고 건수 반환")
    void getUserReportSummary_returns_counts_by_category() {
        given(userReportRepository.countByProductSellerId(1L)).willReturn(2L);
        given(boardPostReportRepository.countByPostAuthorId(1L)).willReturn(1L);
        given(boardCommentReportRepository.countByCommentAuthorId(1L)).willReturn(3L);
        given(userReportRepository.countByReportedUserId(1L)).willReturn(2L);

        UserReportSummaryResponse response = adminService.getUserReportSummary(1L);

        assertThat(response.getProduct()).isEqualTo(2);
        assertThat(response.getPost()).isEqualTo(1);
        assertThat(response.getComment()).isEqualTo(3);
        assertThat(response.getUser()).isEqualTo(2);
    }

    @Test
    @DisplayName("getCohortSummary - 기수별 유저 수 목록 반환")
    void getCohortSummary_returns_cohort_list_with_user_count() {
        List<CohortSummaryResponse> mockData = List.of(
                new CohortSummaryResponse("30기", 15L),
                new CohortSummaryResponse("29기", 22L)
        );
        given(userRepository.findCohortSummary()).willReturn(mockData);

        List<CohortSummaryResponse> result = adminService.getCohortSummary();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getCohort()).isEqualTo("30기");
        assertThat(result.get(0).getUserCount()).isEqualTo(15L);
    }

    @Test
    @DisplayName("getReportsByCohort - 신고 없으면 빈 리스트 반환")
    void getReportsByCohort_returns_empty_when_no_reports() {
        given(userReportRepository.findDistinctReportedUserIds()).willReturn(List.of());
        given(boardPostReportRepository.findDistinctReportedAuthorIds()).willReturn(List.of());
        given(boardCommentReportRepository.findDistinctReportedAuthorIds()).willReturn(List.of());

        List<ReportsByCohortResponse> result = adminService.getReportsByCohort();

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("getReportsByCohort - 신고받은 유저를 기수별로 그룹핑하여 반환")
    void getReportsByCohort_groups_reported_users_by_cohort() {
        User user30 = User.builder()
                .firebaseUid("uid-30").name("김철수").nickname("철수")
                .email("chul@example.com").gender("M").cohort("30기").build();
        User user29 = User.builder()
                .firebaseUid("uid-29").name("이영희").nickname("영희")
                .email("young@example.com").gender("F").cohort("29기").build();

        given(userReportRepository.findDistinctReportedUserIds()).willReturn(List.of(1L));
        given(boardPostReportRepository.findDistinctReportedAuthorIds()).willReturn(List.of(2L));
        given(boardCommentReportRepository.findDistinctReportedAuthorIds()).willReturn(List.of());
        given(userRepository.findAllById(anyCollection())).willReturn(List.of(user30, user29));

        given(userReportRepository.countByProductSellerId(anyLong())).willReturn(0L);
        given(boardPostReportRepository.countByPostAuthorId(anyLong())).willReturn(0L);
        given(boardCommentReportRepository.countByCommentAuthorId(anyLong())).willReturn(0L);
        given(userReportRepository.countByReportedUserId(1L)).willReturn(3L);
        given(userReportRepository.countByReportedUserId(2L)).willReturn(0L);

        List<ReportsByCohortResponse> result = adminService.getReportsByCohort();

        assertThat(result).hasSize(2);
        // 신고 있는 기수만 포함
        result.forEach(cohort -> assertThat(cohort.getReportedUserCount()).isGreaterThan(0));
    }

    @Test
    @DisplayName("getUserActivitySummary - 유저 활동 통계 반환")
    void getUserActivitySummary_returns_activity_counts() {
        given(productRepository.countBySellerId(1L)).willReturn(5L);
        given(transactionRepository.countBySellerIdAndStatus(1L, "completed")).willReturn(3L);
        given(transactionRepository.countByBuyerIdAndStatus(1L, "completed")).willReturn(2L);
        given(boardPostRepository.countByAuthorId(1L)).willReturn(10L);
        given(boardCommentRepository.countByAuthorId(1L)).willReturn(25L);

        UserActivitySummaryResponse response = adminService.getUserActivitySummary(1L);

        assertThat(response.getProductCount()).isEqualTo(5L);
        assertThat(response.getSalesCount()).isEqualTo(3L);
        assertThat(response.getPurchaseCount()).isEqualTo(2L);
        assertThat(response.getPostCount()).isEqualTo(10L);
        assertThat(response.getCommentCount()).isEqualTo(25L);
    }
}
```

- [ ] **Step 2: 테스트 실행 — 컴파일 에러 or 실패 확인**

```bash
./gradlew test --tests "com.example.projectback.admin.service.AdminServiceTest" 2>&1 | tail -20
```
Expected: 컴파일 에러 (AdminService에 신규 메서드 없음) 또는 테스트 FAIL

- [ ] **Step 3: Commit**

```bash
git add src/test/java/com/example/projectback/admin/service/AdminServiceTest.java
git commit -m "test: [admin] 관리자 대시보드 신규 서비스 메서드 단위 테스트 추가"
```

---

### Task 4: AdminService 신규 메서드 구현 (테스트 통과)

**Files:**
- Modify: `src/main/java/com/example/projectback/admin/service/AdminService.java`

**Interfaces:**
- Consumes:
  - `userReportRepository.countByProductSellerId(Long userId): long`
  - `userReportRepository.countByReportedUserId(Long userId): long`
  - `userReportRepository.findDistinctReportedUserIds(): List<Long>`
  - `boardPostReportRepository.countByPostAuthorId(Long userId): long`
  - `boardPostReportRepository.findDistinctReportedAuthorIds(): List<Long>`
  - `boardCommentReportRepository.countByCommentAuthorId(Long userId): long`
  - `boardCommentReportRepository.findDistinctReportedAuthorIds(): List<Long>`
  - `userRepository.findCohortSummary(): List<CohortSummaryResponse>`
  - `userRepository.findByCohort(String cohort, Pageable pageable): Page<User>`
  - `productRepository.countBySellerId(Long sellerId): long`
  - `transactionRepository.countBySellerIdAndStatus(Long sellerId, String status): long`
  - `transactionRepository.countByBuyerIdAndStatus(Long buyerId, String status): long`
  - `boardPostRepository.countByAuthorId(Long authorId): long`
  - `boardCommentRepository.countByAuthorId(Long authorId): long`
- Produces:
  - `getUserReportSummary(Long userId): UserReportSummaryResponse`
  - `getCohortSummary(): List<CohortSummaryResponse>`
  - `getReportsByCohort(): List<ReportsByCohortResponse>`
  - `getUserActivitySummary(Long userId): UserActivitySummaryResponse`
  - `getUsers(Pageable pageable, String cohort): Page<AdminUserResponse>`  ← 기존 시그니처 변경

- [ ] **Step 1: AdminService.java 전체 교체**

```java
package com.example.projectback.admin.service;

import com.example.projectback.admin.dto.*;
import com.example.projectback.board.repository.*;
import com.example.projectback.board.service.BoardPostService;
import com.example.projectback.entity.User;
import com.example.projectback.entity.UserReport;
import com.example.projectback.product.repository.ProductFavoriteRepository;
import com.example.projectback.product.repository.ProductRepository;
import com.example.projectback.product.service.ProductService;
import com.example.projectback.report.UserReportStatus;
import com.example.projectback.report.repository.UserReportRepository;
import com.example.projectback.transaction.repository.TransactionRepository;
import com.example.projectback.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final UserReportRepository userReportRepository;
    private final BoardPostRepository boardPostRepository;
    private final BoardCommentRepository boardCommentRepository;
    private final BoardCommentLikeRepository boardCommentLikeRepository;
    private final BoardCommentReportRepository boardCommentReportRepository;
    private final BoardPostLikeRepository boardPostLikeRepository;
    private final BoardPostReportRepository boardPostReportRepository;
    private final ProductRepository productRepository;
    private final ProductFavoriteRepository productFavoriteRepository;
    private final BoardPostService boardPostService;
    private final ProductService productService;
    private final TransactionRepository transactionRepository;

    @Transactional(readOnly = true)
    public Page<AdminUserResponse> getUsers(Pageable pageable, String cohort) {
        if (cohort != null && !cohort.isBlank()) {
            return userRepository.findByCohort(cohort, pageable).map(AdminUserResponse::from);
        }
        return userRepository.findAll(pageable).map(AdminUserResponse::from);
    }

    @Transactional
    public AdminUserResponse toggleSuspend(Long userId) {
        User user = getUserOrThrow(userId);
        if (Boolean.TRUE.equals(user.getIsSuspended())) {
            user.unsuspend();
        } else {
            user.suspend();
        }
        return AdminUserResponse.from(user);
    }

    @Transactional
    public void forceWithdraw(Long userId) {
        User user = getUserOrThrow(userId);

        List<Long> userCommentIds = boardCommentRepository.findIdsByAuthorId(userId);
        List<Long> childOfUserCommentIds = boardCommentRepository.findReplyIdsByParentAuthorId(userId);
        List<Long> allAffectedCommentIds = new ArrayList<>(userCommentIds);
        allAffectedCommentIds.addAll(childOfUserCommentIds);
        if (!allAffectedCommentIds.isEmpty()) {
            boardCommentLikeRepository.deleteByCommentIdIn(allAffectedCommentIds);
            boardCommentReportRepository.deleteByCommentIdIn(allAffectedCommentIds);
        }
        boardCommentRepository.deleteRepliesByAuthorId(userId);
        boardCommentRepository.deleteParentsByAuthorId(userId);

        boardPostRepository.findIdsByAuthorId(userId).forEach(boardPostService::adminDeletePost);

        boardPostLikeRepository.deleteByUserId(userId);
        boardPostReportRepository.deleteByUserId(userId);

        productRepository.findIdsBySellerId(userId).forEach(productService::adminDeleteProduct);

        productFavoriteRepository.deleteByUserId(userId);

        userReportRepository.deleteByReporterId(userId);
        userReportRepository.deleteByReportedUserId(userId);

        userRepository.delete(user);
    }

    @Transactional(readOnly = true)
    public AdminReportListResponse getReports() {
        List<UserReport> all = userReportRepository.findAllByOrderByCreatedAtDesc();
        long pendingCount = all.stream()
                .filter(r -> r.getStatus() == UserReportStatus.PENDING)
                .count();
        List<AdminReportListResponse.UserReportItem> items = all.stream()
                .map(AdminReportListResponse.UserReportItem::new)
                .toList();
        return new AdminReportListResponse(all.size(), pendingCount, items);
    }

    @Transactional
    public void adminDeletePost(Long postId) {
        boardPostService.adminDeletePost(postId);
    }

    @Transactional
    public void adminDeleteProduct(Long productId) {
        productService.adminDeleteProduct(productId);
    }

    @Transactional(readOnly = true)
    public List<ReportsByCohortResponse> getReportsByCohort() {
        Set<Long> reportedUserIds = new HashSet<>();
        reportedUserIds.addAll(userReportRepository.findDistinctReportedUserIds());
        reportedUserIds.addAll(boardPostReportRepository.findDistinctReportedAuthorIds());
        reportedUserIds.addAll(boardCommentReportRepository.findDistinctReportedAuthorIds());

        if (reportedUserIds.isEmpty()) {
            return List.of();
        }

        List<User> reportedUsers = userRepository.findAllById(reportedUserIds);

        record UserTotal(User user, int total) {}

        List<UserTotal> userTotals = reportedUsers.stream()
                .map(user -> {
                    int product = (int) userReportRepository.countByProductSellerId(user.getId());
                    int post = (int) boardPostReportRepository.countByPostAuthorId(user.getId());
                    int comment = (int) boardCommentReportRepository.countByCommentAuthorId(user.getId());
                    int userReport = (int) userReportRepository.countByReportedUserId(user.getId());
                    return new UserTotal(user, product + post + comment + userReport);
                })
                .filter(ut -> ut.total() > 0)
                .toList();

        return userTotals.stream()
                .collect(Collectors.groupingBy(ut -> ut.user().getCohort()))
                .entrySet().stream()
                .map(entry -> {
                    List<UserTotal> cohortUsers = entry.getValue();
                    int totalReportCount = cohortUsers.stream().mapToInt(UserTotal::total).sum();
                    List<ReportsByCohortResponse.ReportedUserSummary> summaries = cohortUsers.stream()
                            .map(ut -> new ReportsByCohortResponse.ReportedUserSummary(ut.user(), ut.total()))
                            .toList();
                    return new ReportsByCohortResponse(entry.getKey(), cohortUsers.size(), totalReportCount, summaries);
                })
                .sorted(Comparator.comparing(ReportsByCohortResponse::getCohort).reversed())
                .toList();
    }

    @Transactional(readOnly = true)
    public UserReportSummaryResponse getUserReportSummary(Long userId) {
        int product = (int) userReportRepository.countByProductSellerId(userId);
        int post = (int) boardPostReportRepository.countByPostAuthorId(userId);
        int comment = (int) boardCommentReportRepository.countByCommentAuthorId(userId);
        int user = (int) userReportRepository.countByReportedUserId(userId);
        return new UserReportSummaryResponse(product, post, comment, user);
    }

    @Transactional(readOnly = true)
    public List<CohortSummaryResponse> getCohortSummary() {
        return userRepository.findCohortSummary();
    }

    @Transactional(readOnly = true)
    public UserActivitySummaryResponse getUserActivitySummary(Long userId) {
        long productCount = productRepository.countBySellerId(userId);
        long salesCount = transactionRepository.countBySellerIdAndStatus(userId, "completed");
        long purchaseCount = transactionRepository.countByBuyerIdAndStatus(userId, "completed");
        long postCount = boardPostRepository.countByAuthorId(userId);
        long commentCount = boardCommentRepository.countByAuthorId(userId);
        return new UserActivitySummaryResponse(productCount, salesCount, purchaseCount, postCount, commentCount);
    }

    private User getUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("유저를 찾을 수 없습니다."));
    }
}
```

- [ ] **Step 2: 테스트 실행 — 신규 테스트 전체 통과 확인**

```bash
./gradlew test --tests "com.example.projectback.admin.service.AdminServiceTest"
```
Expected: BUILD SUCCESSFUL, 모든 테스트 PASS

- [ ] **Step 3: Commit**

```bash
git add src/main/java/com/example/projectback/admin/service/AdminService.java
git commit -m "feat: [admin] 관리자 대시보드 서비스 메서드 5개 구현"
```

---

### Task 5: AdminController 신규 엔드포인트 추가

**Files:**
- Modify: `src/main/java/com/example/projectback/admin/controller/AdminController.java`

**Interfaces:**
- Consumes:
  - `adminService.getReportsByCohort(): List<ReportsByCohortResponse>`
  - `adminService.getUserReportSummary(Long userId): UserReportSummaryResponse`
  - `adminService.getCohortSummary(): List<CohortSummaryResponse>`
  - `adminService.getUserActivitySummary(Long userId): UserActivitySummaryResponse`
  - `adminService.getUsers(Pageable pageable, String cohort): Page<AdminUserResponse>` ← 시그니처 변경
- Produces: 5개의 신규 REST 엔드포인트

- [ ] **Step 1: AdminController.java 전체 교체**

```java
package com.example.projectback.admin.controller;

import com.example.projectback.admin.dto.*;
import com.example.projectback.admin.service.AdminService;
import com.example.projectback.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/users")
    public ResponseEntity<ApiResponse<Page<AdminUserResponse>>> getUsers(
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable,
            @RequestParam(required = false) String cohort) {
        return ResponseEntity.ok(ApiResponse.success(adminService.getUsers(pageable, cohort), "유저 목록 조회 성공"));
    }

    @PatchMapping("/users/{id}/suspend")
    public ResponseEntity<ApiResponse<AdminUserResponse>> suspendUser(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(adminService.toggleSuspend(id), "유저 정지/해제 성공"));
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id) {
        adminService.forceWithdraw(id);
        return ResponseEntity.ok(ApiResponse.success(null, "유저 강제 탈퇴 성공"));
    }

    @GetMapping("/reports")
    public ResponseEntity<ApiResponse<AdminReportListResponse>> getReports() {
        return ResponseEntity.ok(ApiResponse.success(adminService.getReports(), "신고 목록 조회 성공"));
    }

    @GetMapping("/reports/by-cohort")
    public ResponseEntity<ApiResponse<List<ReportsByCohortResponse>>> getReportsByCohort() {
        return ResponseEntity.ok(ApiResponse.success(adminService.getReportsByCohort(), "기수별 신고 현황 조회 성공"));
    }

    @GetMapping("/users/{userId}/report-summary")
    public ResponseEntity<ApiResponse<UserReportSummaryResponse>> getUserReportSummary(@PathVariable Long userId) {
        return ResponseEntity.ok(ApiResponse.success(adminService.getUserReportSummary(userId), "유저 신고 요약 조회 성공"));
    }

    @GetMapping("/users/cohort-summary")
    public ResponseEntity<ApiResponse<List<CohortSummaryResponse>>> getCohortSummary() {
        return ResponseEntity.ok(ApiResponse.success(adminService.getCohortSummary(), "기수별 가입자 수 조회 성공"));
    }

    @GetMapping("/users/{userId}/activity-summary")
    public ResponseEntity<ApiResponse<UserActivitySummaryResponse>> getUserActivitySummary(@PathVariable Long userId) {
        return ResponseEntity.ok(ApiResponse.success(adminService.getUserActivitySummary(userId), "유저 활동 통계 조회 성공"));
    }

    @DeleteMapping("/posts/{id}")
    public ResponseEntity<ApiResponse<Void>> deletePost(@PathVariable Long id) {
        adminService.adminDeletePost(id);
        return ResponseEntity.ok(ApiResponse.success(null, "게시글 삭제 성공"));
    }

    @DeleteMapping("/products/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(@PathVariable Long id) {
        adminService.adminDeleteProduct(id);
        return ResponseEntity.ok(ApiResponse.success(null, "상품 삭제 성공"));
    }
}
```

> **주의:** `/users/cohort-summary`와 `/users/{userId}/report-summary` 및 `/users/{userId}/activity-summary`는 경로 충돌이 없다. Spring MVC는 literal segment를 variable segment보다 우선하므로 `cohort-summary`가 `{userId}`보다 먼저 매칭된다.

- [ ] **Step 2: 전체 빌드 및 테스트 통과 확인**

```bash
./gradlew build
```
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Commit**

```bash
git add src/main/java/com/example/projectback/admin/controller/AdminController.java
git commit -m "feat: [admin] 관리자 대시보드 API 엔드포인트 5개 추가"
```

---

## Self-Review

### 1. Spec Coverage

| API | Task |
|-----|------|
| GET /admin/reports/by-cohort | Task 4 (서비스), Task 5 (컨트롤러) |
| GET /admin/users/{userId}/report-summary | Task 4, Task 5 |
| GET /admin/users/cohort-summary | Task 4, Task 5 |
| GET /admin/users (cohort 필터) | Task 4 (getUsers 시그니처 변경), Task 5 |
| GET /admin/users/{userId}/activity-summary | Task 4, Task 5 |
| ApiResponse 래퍼 | 모든 컨트롤러 메서드에 적용 |
| ADMIN 권한 | 클래스 레벨 @PreAuthorize 유지 |

모든 스펙 항목이 커버됨.

### 2. Placeholder Scan

없음 — 모든 스텝에 실제 코드 포함.

### 3. Type Consistency

- `CohortSummaryResponse(String cohort, Long userCount)` 생성자 → `findCohortSummary()` JPQL에서 `COUNT(u)` (Long) 전달 → 일치
- `ReportedUserSummary(User user, int totalReportCount)` → AdminService에서 `new ReportedUserSummary(ut.user(), ut.total())` → `ut.total()`은 `int` → 일치
- `getUsers(Pageable, String)` → AdminController에서 `adminService.getUsers(pageable, cohort)` → 일치
- 테스트의 `boardPostRepository.countByAuthorId(1L)` → BoardPostRepository에 이미 `long countByAuthorId(Long authorId)` 존재 → 일치