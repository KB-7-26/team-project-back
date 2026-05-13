package com.example.projectback.board.service;

import com.example.projectback.board.dto.BoardPostCreateRequest;
import com.example.projectback.board.dto.BoardPostCreateResponse;
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
