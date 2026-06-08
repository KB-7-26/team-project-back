package com.example.projectback.board.service;

import com.example.projectback.board.dto.LikeResponse;
import com.example.projectback.board.repository.BoardCommentLikeRepository;
import com.example.projectback.board.repository.BoardCommentRepository;
import com.example.projectback.board.repository.BoardPostLikeRepository;
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

import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BoardLikeServiceTest {

    @Mock private BoardPostLikeRepository boardPostLikeRepository;
    @Mock private BoardCommentLikeRepository boardCommentLikeRepository;
    @Mock private BoardPostRepository boardPostRepository;
    @Mock private BoardCommentRepository boardCommentRepository;
    @Mock private UserRepository userRepository;

    @InjectMocks
    private BoardLikeService boardLikeService;

    @Test
    @DisplayName("게시글 좋아요 추가 - 좋아요가 없으면 저장하고 liked=true 반환")
    void togglePostLike_add() {
        BoardPost post = mock(BoardPost.class);
        User user = mock(User.class);
        given(boardPostRepository.findById(1L)).willReturn(Optional.of(post));
        given(userRepository.findById(10L)).willReturn(Optional.of(user));
        given(boardPostLikeRepository.existsByUserIdAndPostId(10L, 1L)).willReturn(false);
        given(boardPostLikeRepository.countByPostId(1L)).willReturn(1L);

        LikeResponse result = boardLikeService.togglePostLike(1L, 10L);

        assertThat(result.isLiked()).isTrue();
        assertThat(result.getLikeCount()).isEqualTo(1L);
        verify(boardPostLikeRepository).save(any());
    }

    @Test
    @DisplayName("게시글 좋아요 취소 - 좋아요가 이미 있으면 삭제하고 liked=false 반환")
    void togglePostLike_cancel() {
        BoardPost post = mock(BoardPost.class);
        User user = mock(User.class);
        given(boardPostRepository.findById(1L)).willReturn(Optional.of(post));
        given(userRepository.findById(10L)).willReturn(Optional.of(user));
        given(boardPostLikeRepository.existsByUserIdAndPostId(10L, 1L)).willReturn(true);
        given(boardPostLikeRepository.countByPostId(1L)).willReturn(0L);

        LikeResponse result = boardLikeService.togglePostLike(1L, 10L);

        assertThat(result.isLiked()).isFalse();
        assertThat(result.getLikeCount()).isEqualTo(0L);
        verify(boardPostLikeRepository).deleteByUserIdAndPostId(10L, 1L);
    }

    @Test
    @DisplayName("게시글 좋아요 - 게시글이 없으면 NoSuchElementException")
    void togglePostLike_postNotFound() {
        given(boardPostRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> boardLikeService.togglePostLike(999L, 10L))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage("게시글을 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("댓글 좋아요 추가 - 좋아요가 없으면 저장하고 liked=true 반환")
    void toggleCommentLike_add() {
        BoardPost post = mock(BoardPost.class);
        given(post.getId()).willReturn(1L);
        BoardComment comment = mock(BoardComment.class);
        given(comment.getPost()).willReturn(post);
        User user = mock(User.class);

        given(boardCommentRepository.findById(5L)).willReturn(Optional.of(comment));
        given(userRepository.findById(10L)).willReturn(Optional.of(user));
        given(boardCommentLikeRepository.existsByUserIdAndCommentId(10L, 5L)).willReturn(false);
        given(boardCommentLikeRepository.countByCommentId(5L)).willReturn(1L);

        LikeResponse result = boardLikeService.toggleCommentLike(1L, 5L, 10L);

        assertThat(result.isLiked()).isTrue();
        assertThat(result.getLikeCount()).isEqualTo(1L);
        verify(boardCommentLikeRepository).save(any());
    }

    @Test
    @DisplayName("댓글 좋아요 취소 - 좋아요가 이미 있으면 삭제하고 liked=false 반환")
    void toggleCommentLike_cancel() {
        BoardPost post = mock(BoardPost.class);
        given(post.getId()).willReturn(1L);
        BoardComment comment = mock(BoardComment.class);
        given(comment.getPost()).willReturn(post);
        User user = mock(User.class);

        given(boardCommentRepository.findById(5L)).willReturn(Optional.of(comment));
        given(userRepository.findById(10L)).willReturn(Optional.of(user));
        given(boardCommentLikeRepository.existsByUserIdAndCommentId(10L, 5L)).willReturn(true);
        given(boardCommentLikeRepository.countByCommentId(5L)).willReturn(0L);

        LikeResponse result = boardLikeService.toggleCommentLike(1L, 5L, 10L);

        assertThat(result.isLiked()).isFalse();
        assertThat(result.getLikeCount()).isEqualTo(0L);
        verify(boardCommentLikeRepository).deleteByUserIdAndCommentId(10L, 5L);
    }

    @Test
    @DisplayName("댓글 좋아요 - 댓글이 다른 게시글 소속이면 IllegalArgumentException")
    void toggleCommentLike_commentNotBelongsToPost() {
        BoardPost otherPost = mock(BoardPost.class);
        given(otherPost.getId()).willReturn(99L);
        BoardComment comment = mock(BoardComment.class);
        given(comment.getPost()).willReturn(otherPost);

        given(boardCommentRepository.findById(5L)).willReturn(Optional.of(comment));

        assertThatThrownBy(() -> boardLikeService.toggleCommentLike(1L, 5L, 10L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("해당 게시글의 댓글이 아닙니다.");
    }

    @Test
    @DisplayName("댓글 좋아요 - 댓글이 없으면 NoSuchElementException")
    void toggleCommentLike_commentNotFound() {
        given(boardCommentRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> boardLikeService.toggleCommentLike(1L, 999L, 10L))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage("댓글을 찾을 수 없습니다.");
    }
}