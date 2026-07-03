# 댓글 실시간 반영 WebSocket 브로드캐스트 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 게시글 댓글 생성/수정/삭제 시 기존 STOMP 브로커(`/topic`)를 통해 `/topic/board/{postId}/comments`로 실시간 이벤트를 브로드캐스트한다.

**Architecture:** `BoardCommentService`에 `SimpMessagingTemplate`을 주입하고, `createComment`/`updateComment`/`deleteComment`가 DB에 저장/삭제한 직후 새 DTO(`CommentSocketMessage` envelope + `CommentSocketPayload`)를 만들어 `convertAndSend`로 push한다. 인증 체크 없음, 컨트롤러/엔티티/기존 DTO/REST 응답 포맷은 변경하지 않는다.

**Tech Stack:** Spring Boot, Spring WebSocket (STOMP), Lombok, JUnit 5 + Mockito + AssertJ (기존 테스트 스택과 동일)

## Global Constraints

- 토픽 경로는 정확히 `/topic/board/{postId}/comments` (문자열 결합 `"/topic/board/" + postId + "/comments"`)
- 브로드캐스트 payload에 `isOwner`, `liked`, `likeCount`를 포함하지 않는다
- 인증/토큰 체크를 추가하지 않는다
- 좋아요(찜) 토글 로직은 건드리지 않는다
- 기존 REST 응답 DTO(`BoardCommentResponse`), 컨트롤러, 엔티티, `WebSocketConfig`는 수정하지 않는다 (`/topic` 브로커는 이미 활성화되어 있음)
- `CommentSocketPayload`는 `@JsonInclude(JsonInclude.Include.NON_NULL)`로 미사용 필드를 직렬화에서 생략한다

---

### Task 1: DTO 추가 + 댓글 생성(CREATE) 브로드캐스트

**Files:**
- Create: `src/main/java/com/example/projectback/board/dto/CommentSocketPayload.java`
- Create: `src/main/java/com/example/projectback/board/dto/CommentSocketMessage.java`
- Modify: `src/main/java/com/example/projectback/board/service/BoardCommentService.java`
- Test: `src/test/java/com/example/projectback/board/service/BoardCommentServiceTest.java`

**Interfaces:**
- Produces: `CommentSocketPayload` (Lombok `@Builder`, getters: `getCommentId()`, `getParentCommentId()`, `getWriterId()`, `getDisplayName()`, `getContent()`, `getCreatedAt()` — all `Long`/`String`/`LocalDateTime`, nullable)
- Produces: `CommentSocketMessage(String type, CommentSocketPayload comment)` — getters `getType()`, `getComment()`
- Produces: `BoardCommentService` constructor now also takes `SimpMessagingTemplate messagingTemplate` (via `@RequiredArgsConstructor`, no manual constructor change needed)

- [ ] **Step 1: Create `CommentSocketPayload` DTO**

```java
package com.example.projectback.board.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CommentSocketPayload {
    private final Long commentId;
    private final Long parentCommentId;
    private final Long writerId;
    private final String displayName;
    private final String content;
    private final LocalDateTime createdAt;
}
```

- [ ] **Step 2: Create `CommentSocketMessage` DTO**

```java
package com.example.projectback.board.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CommentSocketMessage {
    private final String type;
    private final CommentSocketPayload comment;
}
```

- [ ] **Step 3: Write the failing tests for CREATE broadcast**

Add these imports to `src/test/java/com/example/projectback/board/service/BoardCommentServiceTest.java` (alongside the existing imports):

```java
import com.example.projectback.board.dto.BoardCommentCreateRequest;
import com.example.projectback.board.dto.CommentSocketMessage;
import com.example.projectback.entity.BoardComment;
import com.example.projectback.entity.BoardPost;
import com.example.projectback.entity.User;
import org.mockito.ArgumentCaptor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
```

(`BoardComment`, `BoardPost`, `User`, `List`, `Optional` are already imported in the file — skip duplicates. Add only the ones missing: `CommentSocketMessage`, `ArgumentCaptor`, `SimpMessagingTemplate`, `ReflectionTestUtils`, `LocalDateTime`, `assertThat`, `any`, `doAnswer`, `verify`.)

Add a new mock field next to the other `@Mock` fields:

```java
    @Mock
    private SimpMessagingTemplate messagingTemplate;
```

Add these two tests in the `// ── createComment ─────────────────────────────────────────` section:

```java
    @Test
    @DisplayName("createComment - 저장 성공 시 /topic/board/{postId}/comments로 CREATE 이벤트 브로드캐스트")
    void createComment_success_broadcastsCreateEvent() {
        // given
        Long postId = 1L;
        Long userId = 10L;
        LocalDateTime fixedNow = LocalDateTime.of(2026, 7, 3, 10, 0, 0);

        BoardPost post = mock(BoardPost.class);
        given(post.getAuthor()).willReturn(null);

        User author = mock(User.class);
        given(author.getId()).willReturn(userId);

        BoardCommentCreateRequest request = mock(BoardCommentCreateRequest.class);
        given(request.getParentCommentId()).willReturn(null);
        given(request.getContent()).willReturn("댓글 내용");

        given(boardPostRepository.findById(postId)).willReturn(Optional.of(post));
        given(userRepository.findById(userId)).willReturn(Optional.of(author));
        given(boardCommentRepository.findByPostIdOrderByCreatedAtAsc(postId)).willReturn(List.of());

        doAnswer(invocation -> {
            BoardComment saved = invocation.getArgument(0);
            ReflectionTestUtils.setField(saved, "id", 123L);
            ReflectionTestUtils.setField(saved, "createdAt", fixedNow);
            return saved;
        }).when(boardCommentRepository).save(any(BoardComment.class));

        // when
        boardCommentService.createComment(postId, userId, request);

        // then
        ArgumentCaptor<String> topicCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Object> messageCaptor = ArgumentCaptor.forClass(Object.class);
        verify(messagingTemplate).convertAndSend(topicCaptor.capture(), messageCaptor.capture());

        assertThat(topicCaptor.getValue()).isEqualTo("/topic/board/1/comments");
        CommentSocketMessage message = (CommentSocketMessage) messageCaptor.getValue();
        assertThat(message.getType()).isEqualTo("CREATE");
        assertThat(message.getComment().getCommentId()).isEqualTo(123L);
        assertThat(message.getComment().getParentCommentId()).isNull();
        assertThat(message.getComment().getWriterId()).isEqualTo(10L);
        assertThat(message.getComment().getDisplayName()).isEqualTo("익명");
        assertThat(message.getComment().getContent()).isEqualTo("댓글 내용");
        assertThat(message.getComment().getCreatedAt()).isEqualTo(fixedNow);
    }

    @Test
    @DisplayName("createComment - 대댓글 저장 시 CREATE 이벤트에 parentCommentId 포함")
    void createComment_reply_broadcastsWithParentCommentId() {
        // given
        Long postId = 1L;
        Long userId = 10L;
        Long parentCommentId = 50L;

        BoardPost post = mock(BoardPost.class);
        given(post.getAuthor()).willReturn(null);
        given(post.getId()).willReturn(postId);

        User author = mock(User.class);
        given(author.getId()).willReturn(userId);

        BoardComment parentComment = mock(BoardComment.class);
        given(parentComment.getId()).willReturn(parentCommentId);
        given(parentComment.getPost()).willReturn(post);

        BoardCommentCreateRequest request = mock(BoardCommentCreateRequest.class);
        given(request.getParentCommentId()).willReturn(parentCommentId);
        given(request.getContent()).willReturn("대댓글 내용");

        given(boardPostRepository.findById(postId)).willReturn(Optional.of(post));
        given(userRepository.findById(userId)).willReturn(Optional.of(author));
        given(boardCommentRepository.findById(parentCommentId)).willReturn(Optional.of(parentComment));
        given(boardCommentRepository.findByPostIdOrderByCreatedAtAsc(postId)).willReturn(List.of());

        doAnswer(invocation -> {
            BoardComment saved = invocation.getArgument(0);
            ReflectionTestUtils.setField(saved, "id", 124L);
            ReflectionTestUtils.setField(saved, "createdAt", LocalDateTime.of(2026, 7, 3, 11, 0, 0));
            return saved;
        }).when(boardCommentRepository).save(any(BoardComment.class));

        // when
        boardCommentService.createComment(postId, userId, request);

        // then
        ArgumentCaptor<Object> messageCaptor = ArgumentCaptor.forClass(Object.class);
        verify(messagingTemplate).convertAndSend(org.mockito.ArgumentMatchers.eq("/topic/board/1/comments"), messageCaptor.capture());

        CommentSocketMessage message = (CommentSocketMessage) messageCaptor.getValue();
        assertThat(message.getComment().getParentCommentId()).isEqualTo(50L);
    }
```

- [ ] **Step 4: Run tests to verify they fail**

Run: `./gradlew test --tests "com.example.projectback.board.service.BoardCommentServiceTest"` (Windows: `gradlew.bat test --tests "com.example.projectback.board.service.BoardCommentServiceTest"`)
Expected: `createComment_success_broadcastsCreateEvent` and `createComment_reply_broadcastsWithParentCommentId` FAIL — `messagingTemplate` is never invoked (`Wanted but not invoked`), since `BoardCommentService` doesn't reference `SimpMessagingTemplate` yet (the field also won't compile into `@InjectMocks` until the constructor gains the dependency — compilation still succeeds because Mockito's `@InjectMocks` just won't have a matching field to inject into, it stays unused).

- [ ] **Step 5: Add `SimpMessagingTemplate` field and broadcast call to `createComment`**

In `src/main/java/com/example/projectback/board/service/BoardCommentService.java`, add the import:

```java
import com.example.projectback.board.dto.CommentSocketMessage;
import com.example.projectback.board.dto.CommentSocketPayload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
```

Add the field next to the existing ones:

```java
    private final BoardCommentRepository boardCommentRepository;
    private final BoardCommentLikeRepository boardCommentLikeRepository;
    private final BoardPostRepository boardPostRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;
```

Replace the end of `createComment` — from:

```java
        boardCommentRepository.save(comment);
        log.info("댓글 등록: postId={}, commentId={}, userId={}, isReply={}", postId, comment.getId(), userId, parentComment != null);

        List<BoardComment> allComments = boardCommentRepository.findByPostIdOrderByCreatedAtAsc(postId);
        Map<Long, String> anonMap = buildAnonMap(allComments, getAuthorId(post));

        return new BoardCommentResponse(comment, getDisplayName(comment, anonMap), userId, List.of(), false, 0L);
    }
```

with:

```java
        boardCommentRepository.save(comment);
        log.info("댓글 등록: postId={}, commentId={}, userId={}, isReply={}", postId, comment.getId(), userId, parentComment != null);

        List<BoardComment> allComments = boardCommentRepository.findByPostIdOrderByCreatedAtAsc(postId);
        Map<Long, String> anonMap = buildAnonMap(allComments, getAuthorId(post));
        String displayName = getDisplayName(comment, anonMap);

        messagingTemplate.convertAndSend(
                "/topic/board/" + postId + "/comments",
                new CommentSocketMessage("CREATE", CommentSocketPayload.builder()
                        .commentId(comment.getId())
                        .parentCommentId(parentComment != null ? parentComment.getId() : null)
                        .writerId(userId)
                        .displayName(displayName)
                        .content(comment.getContent())
                        .createdAt(comment.getCreatedAt())
                        .build())
        );

        return new BoardCommentResponse(comment, displayName, userId, List.of(), false, 0L);
    }
```

- [ ] **Step 6: Run tests to verify they pass**

Run: `./gradlew test --tests "com.example.projectback.board.service.BoardCommentServiceTest"`
Expected: PASS (all tests in the class, including the two new ones and the pre-existing error-path tests)

- [ ] **Step 7: Commit**

```bash
git add src/main/java/com/example/projectback/board/dto/CommentSocketPayload.java src/main/java/com/example/projectback/board/dto/CommentSocketMessage.java src/main/java/com/example/projectback/board/service/BoardCommentService.java src/test/java/com/example/projectback/board/service/BoardCommentServiceTest.java
git commit -m "feat: 댓글 생성 시 WebSocket CREATE 이벤트 브로드캐스트"
```

---

### Task 2: 댓글 수정(UPDATE) 브로드캐스트

**Files:**
- Modify: `src/main/java/com/example/projectback/board/service/BoardCommentService.java`
- Test: `src/test/java/com/example/projectback/board/service/BoardCommentServiceTest.java`

**Interfaces:**
- Consumes: `CommentSocketPayload` builder, `CommentSocketMessage(String, CommentSocketPayload)` from Task 1
- Consumes: `messagingTemplate` field from Task 1

- [ ] **Step 1: Write the failing test**

Add to the `// ── updateComment ─────────────────────────────────────────` section of `BoardCommentServiceTest.java`:

```java
    @Test
    @DisplayName("updateComment - 수정 성공 시 /topic/board/{postId}/comments로 UPDATE 이벤트 브로드캐스트")
    void updateComment_success_broadcastsUpdateEvent() {
        // given
        Long postId = 1L;
        Long commentId = 10L;
        Long userId = 1L;

        User author = mock(User.class);
        given(author.getId()).willReturn(userId);

        BoardPost post = mock(BoardPost.class);
        given(post.getId()).willReturn(postId);

        BoardComment comment = mock(BoardComment.class);
        given(comment.getPost()).willReturn(post);
        given(comment.getAuthor()).willReturn(author);

        given(boardPostRepository.findById(postId)).willReturn(Optional.of(post));
        given(boardCommentRepository.findById(commentId)).willReturn(Optional.of(comment));
        given(boardCommentRepository.findByPostIdOrderByCreatedAtAsc(postId)).willReturn(List.of());
        given(boardCommentLikeRepository.existsByUserIdAndCommentId(userId, commentId)).willReturn(false);
        given(boardCommentLikeRepository.countByCommentId(commentId)).willReturn(0L);

        BoardCommentUpdateRequest request = mock(BoardCommentUpdateRequest.class);
        given(request.getContent()).willReturn("수정된 내용");

        // when
        boardCommentService.updateComment(postId, commentId, userId, request);

        // then
        ArgumentCaptor<String> topicCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Object> messageCaptor = ArgumentCaptor.forClass(Object.class);
        verify(messagingTemplate).convertAndSend(topicCaptor.capture(), messageCaptor.capture());

        assertThat(topicCaptor.getValue()).isEqualTo("/topic/board/1/comments");
        CommentSocketMessage message = (CommentSocketMessage) messageCaptor.getValue();
        assertThat(message.getType()).isEqualTo("UPDATE");
        assertThat(message.getComment().getCommentId()).isEqualTo(10L);
        assertThat(message.getComment().getContent()).isEqualTo("수정된 내용");
        assertThat(message.getComment().getParentCommentId()).isNull();
        assertThat(message.getComment().getWriterId()).isNull();
        assertThat(message.getComment().getDisplayName()).isNull();
        assertThat(message.getComment().getCreatedAt()).isNull();
    }
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew test --tests "com.example.projectback.board.service.BoardCommentServiceTest"`
Expected: `updateComment_success_broadcastsUpdateEvent` FAILS with `Wanted but not invoked: messagingTemplate.convertAndSend(...)`

- [ ] **Step 3: Add broadcast call to `updateComment`**

In `BoardCommentService.java`, replace:

```java
        comment.update(request.getContent());
        log.info("댓글 수정: postId={}, commentId={}, userId={}", postId, commentId, userId);

        List<BoardComment> allComments = boardCommentRepository.findByPostIdOrderByCreatedAtAsc(postId);
```

with:

```java
        comment.update(request.getContent());
        log.info("댓글 수정: postId={}, commentId={}, userId={}", postId, commentId, userId);

        messagingTemplate.convertAndSend(
                "/topic/board/" + postId + "/comments",
                new CommentSocketMessage("UPDATE", CommentSocketPayload.builder()
                        .commentId(commentId)
                        .content(request.getContent())
                        .build())
        );

        List<BoardComment> allComments = boardCommentRepository.findByPostIdOrderByCreatedAtAsc(postId);
```

- [ ] **Step 4: Run tests to verify they pass**

Run: `./gradlew test --tests "com.example.projectback.board.service.BoardCommentServiceTest"`
Expected: PASS (all tests)

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/example/projectback/board/service/BoardCommentService.java src/test/java/com/example/projectback/board/service/BoardCommentServiceTest.java
git commit -m "feat: 댓글 수정 시 WebSocket UPDATE 이벤트 브로드캐스트"
```

---

### Task 3: 댓글 삭제(DELETE) 브로드캐스트

**Files:**
- Modify: `src/main/java/com/example/projectback/board/service/BoardCommentService.java`
- Test: `src/test/java/com/example/projectback/board/service/BoardCommentServiceTest.java`

**Interfaces:**
- Consumes: `CommentSocketPayload` builder, `CommentSocketMessage(String, CommentSocketPayload)` from Task 1
- Consumes: `messagingTemplate` field from Task 1

**Known limitation (by design, out of scope):** 대댓글이 있는 부모 댓글을 삭제하면 JPA cascade로 대댓글도 DB에서 삭제되지만, 이 태스크는 부모 댓글에 대한 DELETE 이벤트 1개만 보낸다. 대댓글별 개별 이벤트는 범위 밖.

- [ ] **Step 1: Write the failing test**

Add to the `// ── deleteComment ─────────────────────────────────────────` section of `BoardCommentServiceTest.java`:

```java
    @Test
    @DisplayName("deleteComment - 삭제 성공 시 /topic/board/{postId}/comments로 DELETE 이벤트 브로드캐스트")
    void deleteComment_success_broadcastsDeleteEvent() {
        // given
        Long postId = 1L;
        Long commentId = 10L;
        Long userId = 1L;

        User author = mock(User.class);
        given(author.getId()).willReturn(userId);

        BoardPost post = mock(BoardPost.class);
        given(post.getId()).willReturn(postId);

        BoardComment comment = mock(BoardComment.class);
        given(comment.getPost()).willReturn(post);
        given(comment.getAuthor()).willReturn(author);

        given(boardPostRepository.findById(postId)).willReturn(Optional.of(post));
        given(boardCommentRepository.findById(commentId)).willReturn(Optional.of(comment));

        // when
        boardCommentService.deleteComment(postId, commentId, userId);

        // then
        ArgumentCaptor<String> topicCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Object> messageCaptor = ArgumentCaptor.forClass(Object.class);
        verify(messagingTemplate).convertAndSend(topicCaptor.capture(), messageCaptor.capture());

        assertThat(topicCaptor.getValue()).isEqualTo("/topic/board/1/comments");
        CommentSocketMessage message = (CommentSocketMessage) messageCaptor.getValue();
        assertThat(message.getType()).isEqualTo("DELETE");
        assertThat(message.getComment().getCommentId()).isEqualTo(10L);
        assertThat(message.getComment().getParentCommentId()).isNull();
        assertThat(message.getComment().getWriterId()).isNull();
        assertThat(message.getComment().getDisplayName()).isNull();
        assertThat(message.getComment().getContent()).isNull();
        assertThat(message.getComment().getCreatedAt()).isNull();
    }
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew test --tests "com.example.projectback.board.service.BoardCommentServiceTest"`
Expected: `deleteComment_success_broadcastsDeleteEvent` FAILS with `Wanted but not invoked: messagingTemplate.convertAndSend(...)`

- [ ] **Step 3: Add broadcast call to `deleteComment`**

In `BoardCommentService.java`, replace:

```java
        boardCommentRepository.delete(comment);
        log.info("댓글 삭제: postId={}, commentId={}, userId={}", postId, commentId, userId);
    }
```

with:

```java
        boardCommentRepository.delete(comment);
        log.info("댓글 삭제: postId={}, commentId={}, userId={}", postId, commentId, userId);

        messagingTemplate.convertAndSend(
                "/topic/board/" + postId + "/comments",
                new CommentSocketMessage("DELETE", CommentSocketPayload.builder()
                        .commentId(commentId)
                        .build())
        );
    }
```

- [ ] **Step 4: Run tests to verify they pass**

Run: `./gradlew test --tests "com.example.projectback.board.service.BoardCommentServiceTest"`
Expected: PASS (all tests in the class)

- [ ] **Step 5: Run the full test suite**

Run: `./gradlew test`
Expected: PASS (no regressions elsewhere)

- [ ] **Step 6: Commit**

```bash
git add src/main/java/com/example/projectback/board/service/BoardCommentService.java src/test/java/com/example/projectback/board/service/BoardCommentServiceTest.java
git commit -m "feat: 댓글 삭제 시 WebSocket DELETE 이벤트 브로드캐스트"
```
