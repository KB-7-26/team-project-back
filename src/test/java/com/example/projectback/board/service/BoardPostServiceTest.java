package com.example.projectback.board.service;

import com.example.projectback.board.dto.BoardPostCreateRequest;
import com.example.projectback.board.dto.BoardPostCreateResponse;
import com.example.projectback.board.dto.BoardPostDetailResponse;
import com.example.projectback.board.dto.BoardPostListItemResponse;
import com.example.projectback.board.dto.BoardPostUpdateRequest;
import com.example.projectback.board.repository.BoardCommentRepository;
import com.example.projectback.board.repository.BoardPostLikeRepository;
import com.example.projectback.board.repository.BoardPostRepository;
import com.example.projectback.entity.BoardPost;
import com.example.projectback.entity.User;
import com.example.projectback.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.mockito.ArgumentCaptor;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class BoardPostServiceTest {

    @Mock
    private BoardPostRepository boardPostRepository;

    @Mock
    private BoardCommentRepository boardCommentRepository;

    @Mock
    private BoardPostLikeRepository boardPostLikeRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private BoardPostService boardPostService;

    private User mockUser;

    @BeforeEach
    void setUp() {
        mockUser = mock(User.class);
        lenient().when(boardCommentRepository.countByPostId(anyLong())).thenReturn(0L);
        lenient().when(boardPostLikeRepository.countByPostId(anyLong())).thenReturn(0L);
        lenient().when(boardPostLikeRepository.existsByUserIdAndPostId(anyLong(), anyLong())).thenReturn(false);
    }

    // ── getPosts ──────────────────────────────────────────────

    @Test
    @DisplayName("게시글 목록 조회 - displayName은 항상 '익명' 반환")
    void getPosts_alwaysReturnsAnonymousDisplayName() {
        // given
        User author = mock(User.class);

        BoardPost post = BoardPost.builder()
                .author(author)
                .title("게시글")
                .content("내용")
                .build();

        Pageable pageable = PageRequest.of(0, 10, Sort.by("createdAt").descending());
        given(boardPostRepository.findByOptionalCategory(null, pageable)).willReturn(new PageImpl<>(List.of(post)));

        // when
        Page<BoardPostListItemResponse> result = boardPostService.getPosts(null, "title", null, pageable);

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getDisplayName()).isEqualTo("익명");
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("게시글");
    }

    @Test
    @DisplayName("게시글 목록 조회 - 게시글이 없으면 빈 페이지 반환")
    void getPosts_empty_returnsEmptyPage() {
        // given
        Pageable pageable = PageRequest.of(0, 10, Sort.by("createdAt").descending());
        given(boardPostRepository.findByOptionalCategory(null, pageable)).willReturn(Page.empty());

        // when
        Page<BoardPostListItemResponse> result = boardPostService.getPosts(null, "title", null, pageable);

        // then
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isZero();
    }

    // ── getPostDetail ─────────────────────────────────────────

    @Test
    @DisplayName("게시글 상세 조회 - displayName은 항상 '익명' 반환, incrementViewCount 호출 확인")
    void getPostDetail_success_incrementsViewCount() {
        // given
        User author = mock(User.class);
        given(author.getId()).willReturn(1L);

        BoardPost post = BoardPost.builder()
                .author(author)
                .title("상세 제목")
                .content("상세 내용")
                .build();

        given(boardPostRepository.findById(1L)).willReturn(Optional.of(post));

        // when
        BoardPostDetailResponse response = boardPostService.getPostDetail(1L, 1L);

        // then
        assertThat(response.getTitle()).isEqualTo("상세 제목");
        assertThat(response.getContent()).isEqualTo("상세 내용");
        assertThat(response.getDisplayName()).isEqualTo("익명");
        verify(boardPostRepository, times(1)).incrementViewCount(1L);
    }

    @Test
    @DisplayName("게시글 상세 조회 - 본인 게시글이면 isOwner=true")
    void getPostDetail_owner_isOwnerTrue() {
        // given
        User author = mock(User.class);
        given(author.getId()).willReturn(1L);

        BoardPost post = BoardPost.builder()
                .author(author)
                .title("제목")
                .content("내용")
                .build();

        given(boardPostRepository.findById(1L)).willReturn(Optional.of(post));

        // when
        BoardPostDetailResponse response = boardPostService.getPostDetail(1L, 1L);

        // then
        assertThat(response.isOwner()).isTrue();
    }

    @Test
    @DisplayName("게시글 상세 조회 - 타인 게시글이면 isOwner=false")
    void getPostDetail_notOwner_isOwnerFalse() {
        // given
        User author = mock(User.class);
        given(author.getId()).willReturn(1L);

        BoardPost post = BoardPost.builder()
                .author(author)
                .title("제목")
                .content("내용")
                .build();

        given(boardPostRepository.findById(1L)).willReturn(Optional.of(post));

        // when
        BoardPostDetailResponse response = boardPostService.getPostDetail(1L, 99L);

        // then
        assertThat(response.isOwner()).isFalse();
    }

    @Test
    @DisplayName("게시글 상세 조회 - 비로그인(currentUserId=null)이면 isOwner=false")
    void getPostDetail_notLoggedIn_isOwnerFalse() {
        // given
        User author = mock(User.class);

        BoardPost post = BoardPost.builder()
                .author(author)
                .title("제목")
                .content("내용")
                .build();

        given(boardPostRepository.findById(1L)).willReturn(Optional.of(post));

        // when
        BoardPostDetailResponse response = boardPostService.getPostDetail(1L, null);

        // then
        assertThat(response.isOwner()).isFalse();
    }

    @Test
    @DisplayName("게시글 상세 조회 - 존재하지 않는 게시글이면 예외 발생")
    void getPostDetail_notFound_throwsException() {
        // given
        given(boardPostRepository.findById(999L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> boardPostService.getPostDetail(999L, null))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage("게시글을 찾을 수 없습니다.");
    }

    // ── createPost ───────────────────────────────────────────

    @Test
    @DisplayName("게시글 등록 성공")
    void createPost_success() {
        // given
        BoardPostCreateRequest request = new BoardPostCreateRequest(null, "테스트 제목", "테스트 내용");

        given(userRepository.findById(1L)).willReturn(Optional.of(mockUser));
        given(boardPostRepository.save(any(BoardPost.class))).willAnswer(invocation -> invocation.getArgument(0));

        // when
        BoardPostCreateResponse response = boardPostService.createPost(1L, request);

        // then
        assertThat(response.getTitle()).isEqualTo("테스트 제목");
        verify(boardPostRepository).save(any(BoardPost.class));
    }

    @Test
    @DisplayName("존재하지 않는 userId로 게시글 등록 시 예외 발생")
    void createPost_userNotFound() {
        // given
        BoardPostCreateRequest request = new BoardPostCreateRequest(null, "제목", "내용");
        given(userRepository.findById(999L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> boardPostService.createPost(999L, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("존재하지 않는 사용자입니다.");
    }

    // ── updatePost ───────────────────────────────────────────

    @Test
    @DisplayName("게시글 수정 성공 - 작성자만 수정 가능")
    void updatePost_author_success() {
        // given
        User author = mock(User.class);
        given(author.getId()).willReturn(1L);

        BoardPost post = BoardPost.builder()
                .author(author)
                .title("기존 제목")
                .content("기존 내용")
                .build();

        BoardPostUpdateRequest request = new BoardPostUpdateRequest(null, "수정 제목", "수정 내용");
        given(boardPostRepository.findById(1L)).willReturn(Optional.of(post));

        // when
        BoardPostDetailResponse response = boardPostService.updatePost(1L, 1L, request);

        // then
        assertThat(response.getTitle()).isEqualTo("수정 제목");
        assertThat(response.getContent()).isEqualTo("수정 내용");
        assertThat(response.isOwner()).isTrue();
    }

    @Test
    @DisplayName("게시글 수정 실패 - 작성자가 아니면 권한 예외 발생")
    void updatePost_notAuthor_throwsAccessDeniedException() {
        // given
        User author = mock(User.class);
        given(author.getId()).willReturn(1L);

        BoardPost post = BoardPost.builder()
                .author(author)
                .title("제목")
                .content("내용")
                .build();

        BoardPostUpdateRequest request = new BoardPostUpdateRequest(null, "수정 제목", "수정 내용");
        given(boardPostRepository.findById(1L)).willReturn(Optional.of(post));

        // when & then
        assertThatThrownBy(() -> boardPostService.updatePost(1L, 2L, request))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("게시글 수정/삭제 권한이 없습니다.");
    }

    // ── deletePost ───────────────────────────────────────────

    @Test
    @DisplayName("게시글 삭제 성공 - 작성자만 삭제 가능")
    void deletePost_author_success() {
        // given
        User author = mock(User.class);
        given(author.getId()).willReturn(1L);

        BoardPost post = BoardPost.builder()
                .author(author)
                .title("제목")
                .content("내용")
                .build();

        given(boardPostRepository.findById(1L)).willReturn(Optional.of(post));

        // when
        boardPostService.deletePost(1L, 1L);

        // then
        verify(boardPostRepository).delete(post);
    }

    @Test
    @DisplayName("게시글 삭제 실패 - 존재하지 않는 게시글이면 예외 발생")
    void deletePost_notFound_throwsException() {
        // given
        given(boardPostRepository.findById(999L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> boardPostService.deletePost(999L, 1L))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage("게시글을 찾을 수 없습니다.");
    }

    // ── getPopularPosts ───────────────────────────────────────

    @Test
    @DisplayName("인기글 조회 - 점수 공식 쿼리(findTopByScore)를 호출하고 결과를 반환")
    void getPopularPosts_usesScoreQuery() {
        // given
        User author = mock(User.class);
        BoardPost post = BoardPost.builder()
                .author(author)
                .title("인기글")
                .content("내용")
                .build();

        given(boardPostRepository.findTopByScore(any(LocalDateTime.class), any(Pageable.class)))
                .willReturn(List.of(post));

        // when
        List<BoardPostListItemResponse> result = boardPostService.getPopularPosts(10);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("인기글");
        verify(boardPostRepository).findTopByScore(any(LocalDateTime.class), any(Pageable.class));
    }

    @Test
    @DisplayName("인기글 조회 - 7일 이내 기준 날짜를 전달하고 limit 크기로 Pageable 요청")
    void getPopularPosts_passes7DaysAgoAndCorrectLimit() {
        // given
        given(boardPostRepository.findTopByScore(any(LocalDateTime.class), any(Pageable.class)))
                .willReturn(List.of());

        // when
        boardPostService.getPopularPosts(20);

        // then
        ArgumentCaptor<LocalDateTime> sinceCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(boardPostRepository).findTopByScore(sinceCaptor.capture(), pageableCaptor.capture());

        assertThat(sinceCaptor.getValue()).isAfter(LocalDateTime.now().minusDays(7).minusSeconds(5));
        assertThat(pageableCaptor.getValue().getPageSize()).isEqualTo(20);
    }

}