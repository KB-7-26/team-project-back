package com.example.projectback.user.service;

import com.example.projectback.board.dto.BoardPostListItemResponse;
import com.example.projectback.board.repository.BoardCommentRepository;
import com.example.projectback.board.repository.BoardPostImageRepository;
import com.example.projectback.board.repository.BoardPostLikeRepository;
import com.example.projectback.board.repository.BoardPostRepository;
import com.example.projectback.entity.BoardPost;
import com.example.projectback.security.CurrentUserProvider;
import com.example.projectback.user.dto.BoardEventDailyStatsResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UserBoardActivityServiceTest {

    @Mock
    private BoardPostRepository boardPostRepository;

    @Mock
    private BoardCommentRepository boardCommentRepository;

    @Mock
    private BoardPostLikeRepository boardPostLikeRepository;

    @Mock
    private BoardPostImageRepository boardPostImageRepository;

    @Mock
    private CurrentUserProvider currentUserProvider;

    @InjectMocks
    private UserBoardActivityService userBoardActivityService;

    @Test
    @DisplayName("내가 쓴 글 조회 - 현재 사용자 작성글을 게시글 목록 응답으로 반환")
    void getMyPosts_returnsCurrentUserPosts() {
        // given
        Long userId = 1L;
        Pageable pageable = PageRequest.of(0, 10);
        BoardPost post = mockPost(10L, "내가 쓴 글");

        given(currentUserProvider.getCurrentUserId()).willReturn(userId);
        given(boardPostRepository.findByAuthorId(userId, pageable))
                .willReturn(new PageImpl<>(List.of(post), pageable, 1));
        lenient().when(boardCommentRepository.countByPostId(anyLong())).thenReturn(0L);
        lenient().when(boardPostLikeRepository.countByPostId(anyLong())).thenReturn(0L);
        lenient().when(boardPostImageRepository.countByPostId(anyLong())).thenReturn(0);

        // when
        Page<BoardPostListItemResponse> result = userBoardActivityService.getMyPosts(pageable);

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getId()).isEqualTo(10L);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("내가 쓴 글");
        verify(boardPostRepository).findByAuthorId(userId, pageable);
    }

    @Test
    @DisplayName("댓글 단 글 조회 - 현재 사용자가 댓글을 단 게시글을 목록 응답으로 반환")
    void getMyCommentedPosts_returnsCurrentUserCommentedPosts() {
        // given
        Long userId = 1L;
        Pageable pageable = PageRequest.of(0, 10);
        BoardPost post = mockPost(20L, "댓글 단 글");

        given(currentUserProvider.getCurrentUserId()).willReturn(userId);
        given(boardPostRepository.findCommentedPostsByAuthorId(userId, pageable))
                .willReturn(new PageImpl<>(List.of(post), pageable, 1));
        lenient().when(boardCommentRepository.countByPostId(anyLong())).thenReturn(0L);
        lenient().when(boardPostLikeRepository.countByPostId(anyLong())).thenReturn(0L);
        lenient().when(boardPostImageRepository.countByPostId(anyLong())).thenReturn(0);

        // when
        Page<BoardPostListItemResponse> result = userBoardActivityService.getMyCommentedPosts(pageable);

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getId()).isEqualTo(20L);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("댓글 단 글");
        verify(boardPostRepository).findCommentedPostsByAuthorId(userId, pageable);
    }

    @Test
    @DisplayName("추천한 글 조회 - 현재 사용자가 추천한 게시글을 목록 응답으로 반환")
    void getMyLikedPosts_returnsCurrentUserLikedPosts() {
        // given
        Long userId = 1L;
        Pageable pageable = PageRequest.of(0, 10);
        BoardPost post = mockPost(30L, "추천한 글");

        given(currentUserProvider.getCurrentUserId()).willReturn(userId);
        given(boardPostRepository.findLikedPostsByUserId(userId, pageable))
                .willReturn(new PageImpl<>(List.of(post), pageable, 1));
        lenient().when(boardCommentRepository.countByPostId(anyLong())).thenReturn(0L);
        lenient().when(boardPostLikeRepository.countByPostId(anyLong())).thenReturn(0L);

        // when
        Page<BoardPostListItemResponse> result = userBoardActivityService.getMyLikedPosts(pageable);

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getId()).isEqualTo(30L);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("추천한 글");
        verify(boardPostRepository).findLikedPostsByUserId(userId, pageable);
    }

    @Test
    @DisplayName("이벤트 활동 조회 - 7월 7일부터 10일까지 작성글과 댓글 수를 날짜별로 반환")
    void getBoardEventDailyStats_returnsEventPeriodCounts() {
        // given
        Long userId = 1L;
        given(currentUserProvider.getCurrentUserId()).willReturn(userId);
        given(boardPostRepository.countByAuthorIdAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(
                anyLong(), any(LocalDateTime.class), any(LocalDateTime.class)
        )).willAnswer(invocation -> ((LocalDateTime) invocation.getArgument(1)).getDayOfMonth() - 6L);
        given(boardCommentRepository.countByAuthorIdAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(
                anyLong(), any(LocalDateTime.class), any(LocalDateTime.class)
        )).willAnswer(invocation -> ((LocalDateTime) invocation.getArgument(1)).getDayOfMonth() - 7L);

        // when
        List<BoardEventDailyStatsResponse> result = userBoardActivityService.getBoardEventDailyStats();

        // then
        assertThat(result).hasSize(4);
        assertThat(result).extracting(BoardEventDailyStatsResponse::getDay)
                .containsExactly(7, 8, 9, 10);
        assertThat(result).extracting(BoardEventDailyStatsResponse::getPostCount)
                .containsExactly(1L, 2L, 3L, 4L);
        assertThat(result).extracting(BoardEventDailyStatsResponse::getCommentCount)
                .containsExactly(0L, 1L, 2L, 3L);
        assertThat(result).extracting(BoardEventDailyStatsResponse::getTotalCount)
                .containsExactly(1L, 3L, 5L, 7L);
    }

    private BoardPost mockPost(Long id, String title) {
        BoardPost post = mock(BoardPost.class);
        given(post.getId()).willReturn(id);
        given(post.getTitle()).willReturn(title);
        given(post.getViewCount()).willReturn(0);
        given(post.getCreatedAt()).willReturn(LocalDateTime.of(2026, 6, 4, 12, 0));
        return post;
    }
}
