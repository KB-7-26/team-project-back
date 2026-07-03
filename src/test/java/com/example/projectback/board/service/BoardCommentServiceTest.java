package com.example.projectback.board.service;

import com.example.projectback.board.dto.BoardCommentCreateRequest;
import com.example.projectback.board.dto.BoardCommentUpdateRequest;
import com.example.projectback.board.dto.CommentSocketMessage;
import com.example.projectback.board.repository.BoardCommentLikeRepository;
import com.example.projectback.board.repository.BoardCommentRepository;
import com.example.projectback.board.repository.BoardPostRepository;
import com.example.projectback.entity.BoardComment;
import com.example.projectback.entity.BoardPost;
import com.example.projectback.entity.User;
import com.example.projectback.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class BoardCommentServiceTest {

    @Mock
    private BoardCommentRepository boardCommentRepository;

    @Mock
    private BoardCommentLikeRepository boardCommentLikeRepository;

    @Mock
    private BoardPostRepository boardPostRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private BoardCommentService boardCommentService;

    // ── createComment ─────────────────────────────────────────

    @Test
    @DisplayName("createComment - 부모 댓글이 다른 게시글 소속이면 IllegalArgumentException")
    void createComment_parentBelongsToAnotherPost_throwsIllegalArgumentException() {
        // given
        Long postId = 1L;
        Long userId = 1L;
        Long parentCommentId = 99L;

        BoardPost anotherPost = mock(BoardPost.class);
        given(anotherPost.getId()).willReturn(2L);

        BoardComment parentComment = mock(BoardComment.class);
        given(parentComment.getPost()).willReturn(anotherPost);

        given(boardPostRepository.findById(postId)).willReturn(Optional.of(mock(BoardPost.class)));
        given(userRepository.findById(userId)).willReturn(Optional.of(mock(User.class)));
        given(boardCommentRepository.findById(parentCommentId)).willReturn(Optional.of(parentComment));

        BoardCommentCreateRequest request = mock(BoardCommentCreateRequest.class);
        given(request.getParentCommentId()).willReturn(parentCommentId);

        // when & then
        assertThatThrownBy(() -> boardCommentService.createComment(postId, userId, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("부모 댓글이 해당 게시글에 속하지 않습니다.");
    }

    @Test
    @DisplayName("createComment - 존재하지 않는 parentCommentId이면 NoSuchElementException")
    void createComment_parentCommentNotFound_throwsNoSuchElementException() {
        // given
        Long postId = 1L;
        Long userId = 1L;
        Long parentCommentId = 999L;

        given(boardPostRepository.findById(postId)).willReturn(Optional.of(mock(BoardPost.class)));
        given(userRepository.findById(userId)).willReturn(Optional.of(mock(User.class)));
        given(boardCommentRepository.findById(parentCommentId)).willReturn(Optional.empty());

        BoardCommentCreateRequest request = mock(BoardCommentCreateRequest.class);
        given(request.getParentCommentId()).willReturn(parentCommentId);

        // when & then
        assertThatThrownBy(() -> boardCommentService.createComment(postId, userId, request))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage("댓글을 찾을 수 없습니다.");
    }

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
        verify(messagingTemplate).convertAndSend(eq("/topic/board/1/comments"), messageCaptor.capture());

        CommentSocketMessage message = (CommentSocketMessage) messageCaptor.getValue();
        assertThat(message.getComment().getParentCommentId()).isEqualTo(50L);
    }

    // ── updateComment ─────────────────────────────────────────

    @Test
    @DisplayName("updateComment - 댓글이 다른 게시글 소속이면 IllegalArgumentException")
    void updateComment_commentBelongsToAnotherPost_throwsIllegalArgumentException() {
        // given
        Long postId = 1L;
        Long commentId = 10L;
        Long userId = 1L;

        BoardPost anotherPost = mock(BoardPost.class);
        given(anotherPost.getId()).willReturn(2L);

        BoardComment comment = mock(BoardComment.class);
        given(comment.getPost()).willReturn(anotherPost);

        given(boardPostRepository.findById(postId)).willReturn(Optional.of(mock(BoardPost.class)));
        given(boardCommentRepository.findById(commentId)).willReturn(Optional.of(comment));

        BoardCommentUpdateRequest request = mock(BoardCommentUpdateRequest.class);

        // when & then
        assertThatThrownBy(() -> boardCommentService.updateComment(postId, commentId, userId, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("해당 게시글의 댓글이 아닙니다.");
    }

    @Test
    @DisplayName("updateComment - 작성자가 아니면 AccessDeniedException")
    void updateComment_notAuthor_throwsAccessDeniedException() {
        // given
        Long postId = 1L;
        Long commentId = 10L;
        Long userId = 99L;

        User author = mock(User.class);
        given(author.getId()).willReturn(1L);

        BoardPost post = mock(BoardPost.class);
        given(post.getId()).willReturn(postId);

        BoardComment comment = mock(BoardComment.class);
        given(comment.getPost()).willReturn(post);
        given(comment.getAuthor()).willReturn(author);

        given(boardPostRepository.findById(postId)).willReturn(Optional.of(post));
        given(boardCommentRepository.findById(commentId)).willReturn(Optional.of(comment));

        BoardCommentUpdateRequest request = mock(BoardCommentUpdateRequest.class);

        // when & then
        assertThatThrownBy(() -> boardCommentService.updateComment(postId, commentId, userId, request))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("댓글 수정/삭제 권한이 없습니다.");
    }

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

    // ── deleteComment ─────────────────────────────────────────

    @Test
    @DisplayName("deleteComment - 댓글이 다른 게시글 소속이면 IllegalArgumentException")
    void deleteComment_commentBelongsToAnotherPost_throwsIllegalArgumentException() {
        // given
        Long postId = 1L;
        Long commentId = 10L;
        Long userId = 1L;

        BoardPost anotherPost = mock(BoardPost.class);
        given(anotherPost.getId()).willReturn(2L);

        BoardComment comment = mock(BoardComment.class);
        given(comment.getPost()).willReturn(anotherPost);

        given(boardPostRepository.findById(postId)).willReturn(Optional.of(mock(BoardPost.class)));
        given(boardCommentRepository.findById(commentId)).willReturn(Optional.of(comment));

        // when & then
        assertThatThrownBy(() -> boardCommentService.deleteComment(postId, commentId, userId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("해당 게시글의 댓글이 아닙니다.");
    }

    @Test
    @DisplayName("deleteComment - 작성자가 아니면 AccessDeniedException")
    void deleteComment_notAuthor_throwsAccessDeniedException() {
        // given
        Long postId = 1L;
        Long commentId = 10L;
        Long userId = 99L;

        User author = mock(User.class);
        given(author.getId()).willReturn(1L);

        BoardPost post = mock(BoardPost.class);
        given(post.getId()).willReturn(postId);

        BoardComment comment = mock(BoardComment.class);
        given(comment.getPost()).willReturn(post);
        given(comment.getAuthor()).willReturn(author);

        given(boardPostRepository.findById(postId)).willReturn(Optional.of(post));
        given(boardCommentRepository.findById(commentId)).willReturn(Optional.of(comment));

        // when & then
        assertThatThrownBy(() -> boardCommentService.deleteComment(postId, commentId, userId))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("댓글 수정/삭제 권한이 없습니다.");
    }

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
}