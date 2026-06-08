package com.example.projectback.board.service;

import com.example.projectback.board.dto.BoardCommentCreateRequest;
import com.example.projectback.board.dto.BoardCommentUpdateRequest;
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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

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
}