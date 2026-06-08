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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;

@Slf4j
@Service
@RequiredArgsConstructor
public class BoardPostService {

    private final BoardPostRepository boardPostRepository;
    private final BoardCommentRepository boardCommentRepository;
    private final BoardPostLikeRepository boardPostLikeRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public Page<BoardPostListItemResponse> getPosts(Pageable pageable) {
        return boardPostRepository.findAll(pageable)
                .map(post -> new BoardPostListItemResponse(
                        post,
                        boardCommentRepository.countByPostId(post.getId()),
                        boardPostLikeRepository.countByPostId(post.getId())
                ));
    }

    @Transactional
    public BoardPostDetailResponse getPostDetail(Long postId, Long currentUserId) {
        getPostOrThrow(postId);
        boardPostRepository.incrementViewCount(postId);
        BoardPost post = getPostOrThrow(postId);
        boolean liked = currentUserId != null && boardPostLikeRepository.existsByUserIdAndPostId(currentUserId, postId);
        long likeCount = boardPostLikeRepository.countByPostId(postId);
        return new BoardPostDetailResponse(post, currentUserId, liked, likeCount);
    }

    @Transactional
    public BoardPostCreateResponse createPost(Long userId, BoardPostCreateRequest request) {
        User author = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        BoardPost post = BoardPost.builder()
                .author(author)
                .title(request.getTitle())
                .content(request.getContent())
                .build();

        boardPostRepository.save(post);
        log.info("게시글 등록: postId={}, userId={}", post.getId(), userId);

        return new BoardPostCreateResponse(post.getId(), post.getTitle(), post.getCreatedAt());
    }

    @Transactional
    public BoardPostDetailResponse updatePost(Long postId, Long userId, BoardPostUpdateRequest request) {
        BoardPost post = getPostOrThrow(postId);
        validateAuthor(post, userId);

        post.update(request.getTitle(), request.getContent());
        log.info("게시글 수정: postId={}, userId={}", postId, userId);

        boolean liked = boardPostLikeRepository.existsByUserIdAndPostId(userId, postId);
        long likeCount = boardPostLikeRepository.countByPostId(postId);
        return new BoardPostDetailResponse(post, userId, liked, likeCount);
    }

    @Transactional
    public void deletePost(Long postId, Long userId) {
        BoardPost post = getPostOrThrow(postId);
        validateAuthor(post, userId);

        boardPostRepository.delete(post);
        log.info("게시글 삭제: postId={}, userId={}", postId, userId);
    }

    private BoardPost getPostOrThrow(Long postId) {
        return boardPostRepository.findById(postId)
                .orElseThrow(() -> new NoSuchElementException("게시글을 찾을 수 없습니다."));
    }

    private void validateAuthor(BoardPost post, Long userId) {
        if (userId == null || !userId.equals(post.getAuthor().getId())) {
            log.warn("게시글 수정/삭제 권한 없음: postId={}, userId={}", post.getId(), userId);
            throw new AccessDeniedException("게시글 수정/삭제 권한이 없습니다.");
        }
    }
}