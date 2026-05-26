package com.example.projectback.board.service;

import com.example.projectback.board.dto.BoardPostCreateRequest;
import com.example.projectback.board.dto.BoardPostCreateResponse;
import com.example.projectback.board.dto.BoardPostDetailResponse;
import com.example.projectback.board.dto.BoardPostListItemResponse;
import com.example.projectback.board.dto.BoardPostUpdateRequest;
import com.example.projectback.board.repository.BoardPostRepository;
import com.example.projectback.entity.BoardPost;
import com.example.projectback.entity.User;
import com.example.projectback.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class BoardPostService {

    private final BoardPostRepository boardPostRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public Page<BoardPostListItemResponse> getPosts(Pageable pageable) {
        return boardPostRepository.findAll(pageable)
                .map(BoardPostListItemResponse::new);
    }

    @Transactional
    public BoardPostDetailResponse getPostDetail(Long postId, Long currentUserId) {
        BoardPost post = boardPostRepository.findById(postId)
                .orElseThrow(() -> new NoSuchElementException("게시글을 찾을 수 없습니다."));
        post.incrementViewCount();
        return new BoardPostDetailResponse(post, currentUserId);
    }

    @Transactional
    public BoardPostCreateResponse createPost(Long userId, BoardPostCreateRequest request) {
        User author = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        BoardPost post = BoardPost.builder()
                .author(author)
                .title(request.getTitle())
                .content(request.getContent())
                .isAnonymous(request.getIsAnonymous() != null ? request.getIsAnonymous() : false)
                .viewCount(0)
                .build();

        boardPostRepository.save(post);

        return new BoardPostCreateResponse(post.getId(), post.getTitle(), post.getCreatedAt());
    }

    @Transactional
    public BoardPostDetailResponse updatePost(Long postId, Long userId, BoardPostUpdateRequest request) {
        BoardPost post = getPostOrThrow(postId);
        validateAuthor(post, userId);

        post.update(request.getTitle(), request.getContent(), request.getIsAnonymous());
        return new BoardPostDetailResponse(post, userId);
    }

    @Transactional
    public void deletePost(Long postId, Long userId) {
        BoardPost post = getPostOrThrow(postId);
        validateAuthor(post, userId);

        boardPostRepository.delete(post);
    }

    private BoardPost getPostOrThrow(Long postId) {
        return boardPostRepository.findById(postId)
                .orElseThrow(() -> new NoSuchElementException("게시글을 찾을 수 없습니다."));
    }

    private void validateAuthor(BoardPost post, Long userId) {
        if (userId == null || !userId.equals(post.getAuthor().getId())) {
            throw new AccessDeniedException("게시글 수정/삭제 권한이 없습니다.");
        }
    }
}
