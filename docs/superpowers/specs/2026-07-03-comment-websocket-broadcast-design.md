# 댓글 실시간 반영 WebSocket 브로드캐스트 설계

## 배경

게시글 댓글은 현재 REST API로만 동작해서, 다른 사용자가 댓글을 쓰거나 수정/삭제해도 새로고침 전까지 화면에 반영되지 않는다. 이미 채팅 기능에서 쓰고 있는 STOMP 브로커(`/topic` prefix, `SimpMessagingTemplate`)를 그대로 재사용해서 댓글 변경 사항을 실시간으로 push한다.

## 범위

- 댓글 생성(`POST /api/posts/{id}/comments`), 수정(`PUT /api/posts/{id}/comments/{commentId}`), 삭제(`DELETE /api/posts/{id}/comments/{commentId}`) 시 WebSocket 브로드캐스트 추가
- 좋아요(찜) 토글 브로드캐스트는 범위 밖
- 인증 불필요 (기존 댓글 조회처럼 공개)

## 토픽

```
/topic/board/{postId}/comments
```

`WebSocketConfig`에 이미 `/topic` 브로커가 활성화되어 있어 별도 설정 변경 없음.

## 메시지 포맷

envelope + payload 구조. `comment` 필드는 타입별로 필요한 필드만 채우고 나머지는 직렬화에서 생략한다(`@JsonInclude(NON_NULL)`).

**CREATE**
```json
{
  "type": "CREATE",
  "comment": {
    "commentId": 123,
    "parentCommentId": null,
    "writerId": 45,
    "displayName": "익명123",
    "content": "댓글 내용",
    "createdAt": "2026-07-03T10:00:00"
  }
}
```

**UPDATE**
```json
{ "type": "UPDATE", "comment": { "commentId": 123, "content": "수정된 내용" } }
```

**DELETE**
```json
{ "type": "DELETE", "comment": { "commentId": 123 } }
```

`isOwner`, `liked`, `likeCount`는 포함하지 않는다 (보는 사람마다 달라 프론트에서 계산).

## 구현 지점

새 DTO 2개 추가 (`board/dto` 패키지):
- `CommentSocketMessage` — `{ type: String, comment: CommentSocketPayload }`
- `CommentSocketPayload` — `commentId, parentCommentId, writerId, displayName, content, createdAt` 전체 필드를 갖되 `@JsonInclude(NON_NULL)`로 미사용 필드는 직렬화에서 생략

`BoardCommentService`만 수정 (기존 로직/리턴값/REST 응답 포맷 변경 없음, 순수 추가):
- `SimpMessagingTemplate` 필드 추가 (생성자 주입)
- `createComment`: `boardCommentRepository.save(comment)` 직후, 기존 `anonMap`/`getDisplayName()` 로직을 재사용해 CREATE 페이로드 구성 후 전송
- `updateComment`: `comment.update(...)` 직후, `commentId`+`content`만 담아 UPDATE 전송
- `deleteComment`: `boardCommentRepository.delete(comment)` 직후, `commentId`만 담아 DELETE 전송

## 알려진 제약사항

대댓글이 달린 부모 댓글을 삭제하면 JPA cascade(`orphanRemoval`)로 대댓글도 DB에서 함께 삭제되지만, 이번 범위에서는 부모 댓글에 대한 DELETE 이벤트 1개만 전송한다. 대댓글 각각에 대한 이벤트는 범위 밖으로 둔다.

## 테스트

- `BoardCommentService`에 대해 각 메서드 호출 시 `messagingTemplate.convertAndSend`가 올바른 토픽/페이로드로 호출되는지 단위 테스트(Mockito) 추가