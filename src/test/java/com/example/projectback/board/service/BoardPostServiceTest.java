package com.example.projectback.board.service;

import com.example.projectback.board.dto.BoardPostCreateRequest;
import com.example.projectback.board.dto.BoardPostCreateResponse;
import com.example.projectback.board.dto.BoardPostListItemResponse;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class BoardPostServiceTest {

    @Mock
    private BoardPostRepository boardPostRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private BoardPostService boardPostService;

    private User mockUser;

    @BeforeEach
    void setUp() {
        mockUser = mock(User.class);
    }

    // ── getPosts ──────────────────────────────────────────────

    @Test
    @DisplayName("게시글 목록 조회 - 일반 게시글은 닉네임 반환")
    void getPosts_nonAnonymous_returnsNickname() {
        // given
        User author = mock(User.class);
        given(author.getNickname()).willReturn("홍길동");

        BoardPost post = BoardPost.builder()
                .author(author)
                .title("일반 게시글")
                .content("내용")
                .isAnonymous(false)
                .viewCount(0)
                .build();

        Pageable pageable = PageRequest.of(0, 10, Sort.by("createdAt").descending());
        given(boardPostRepository.findAll(pageable)).willReturn(new PageImpl<>(List.of(post)));

        // when
        Page<BoardPostListItemResponse> result = boardPostService.getPosts(pageable);

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getNickname()).isEqualTo("홍길동");
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("일반 게시글");
    }

    @Test
    @DisplayName("게시글 목록 조회 - 익명 게시글은 닉네임을 '익명'으로 반환")
    void getPosts_anonymous_returnsAnonymous() {
        // given
        User author = mock(User.class);

        BoardPost post = BoardPost.builder()
                .author(author)
                .title("익명 게시글")
                .content("내용")
                .isAnonymous(true)
                .viewCount(0)
                .build();

        Pageable pageable = PageRequest.of(0, 10, Sort.by("createdAt").descending());
        given(boardPostRepository.findAll(pageable)).willReturn(new PageImpl<>(List.of(post)));

        // when
        Page<BoardPostListItemResponse> result = boardPostService.getPosts(pageable);

        // then
        assertThat(result.getContent().get(0).getNickname()).isEqualTo("익명");
    }

    @Test
    @DisplayName("게시글 목록 조회 - 게시글이 없으면 빈 페이지 반환")
    void getPosts_empty_returnsEmptyPage() {
        // given
        Pageable pageable = PageRequest.of(0, 10, Sort.by("createdAt").descending());
        given(boardPostRepository.findAll(pageable)).willReturn(Page.empty());

        // when
        Page<BoardPostListItemResponse> result = boardPostService.getPosts(pageable);

        // then
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isZero();
    }

    // ── createPost ───────────────────────────────────────────

    @Test
    @DisplayName("게시글 등록 성공")
    void createPost_success() {
        // given
        BoardPostCreateRequest request = new BoardPostCreateRequest("테스트 제목", "테스트 내용", false);

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
        BoardPostCreateRequest request = new BoardPostCreateRequest("제목", "내용", false);
        given(userRepository.findById(999L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> boardPostService.createPost(999L, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("존재하지 않는 사용자입니다.");
    }

    @Test
    @DisplayName("isAnonymous가 null이면 false로 처리")
    void createPost_isAnonymousNull_defaultFalse() {
        // given
        BoardPostCreateRequest request = new BoardPostCreateRequest("제목", "내용", null);
        given(userRepository.findById(1L)).willReturn(Optional.of(mockUser));
        given(boardPostRepository.save(any(BoardPost.class))).willAnswer(invocation -> {
            BoardPost post = invocation.getArgument(0);
            assertThat(post.getIsAnonymous()).isFalse();
            return post;
        });

        // when
        boardPostService.createPost(1L, request);

        // then
        verify(boardPostRepository).save(any(BoardPost.class));
    }
}
