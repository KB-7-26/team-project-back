package com.example.projectback.board.service;

import com.example.projectback.board.dto.*;
import com.example.projectback.board.repository.*;
import com.example.projectback.entity.BoardPost;
import com.example.projectback.entity.BoardPostImage;
import com.example.projectback.entity.User;
import com.example.projectback.image.service.ImageStorageService;
import com.example.projectback.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.domain.PageRequest;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;


@Slf4j
@Service
@RequiredArgsConstructor
public class BoardPostService {

    private final BoardPostRepository boardPostRepository;
    private final BoardCommentRepository boardCommentRepository;
    private final BoardCommentLikeRepository boardCommentLikeRepository;
    private final BoardCommentReportRepository boardCommentReportRepository;
    private final BoardPostLikeRepository boardPostLikeRepository;
    private final BoardPostReportRepository boardPostReportRepository;
    private final UserRepository userRepository;
    private final BoardPostImageRepository boardPostImageRepository;
    private final ImageStorageService imageStorageService;

    public static final List<String> CATEGORIES = List.of("자유게시판", "공지", "전공", "비전공", "취업");

    @Transactional(readOnly = true)
    public List<String> getCategories() {
        return CATEGORIES;
    }

    @Transactional(readOnly = true)
    public Page<BoardPostListItemResponse> getPosts(String keyword, String searchType, String category, Pageable pageable) {
        String validCategory = (category != null && CATEGORIES.contains(category)) ? category : null;
        Page<BoardPost> page;
        if (keyword == null || keyword.isBlank()) {
            page = boardPostRepository.findByOptionalCategory(validCategory, pageable);
        } else if ("all".equalsIgnoreCase(searchType)) {
            page = boardPostRepository.findByOptionalCategoryAndTitleOrContent(validCategory, keyword, pageable);
        } else {
            page = boardPostRepository.findByOptionalCategoryAndTitle(validCategory, keyword, pageable);
        }
        return page.map(post -> new BoardPostListItemResponse(
                post,
                boardCommentRepository.countByPostId(post.getId()),
                boardPostLikeRepository.countByPostId(post.getId())
        ));
    }

    @Transactional(readOnly = true)
    public List<BoardPostListItemResponse> getPopularPosts(int limit) {
        LocalDateTime since = LocalDateTime.now().minusDays(7);
        return boardPostRepository.findTopByScore(since, PageRequest.of(0, limit))
                .stream()
                .map(post -> new BoardPostListItemResponse(
                        post,
                        boardCommentRepository.countByPostId(post.getId()),
                        boardPostLikeRepository.countByPostId(post.getId())
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<BoardPostListItemResponse> getMostViewedPosts(int limit) {
        return boardPostRepository.findAllByOrderByViewCountDescCreatedAtDesc(PageRequest.of(0, limit))
                .stream()
                .map(post -> new BoardPostListItemResponse(
                        post,
                        boardCommentRepository.countByPostId(post.getId()),
                        boardPostLikeRepository.countByPostId(post.getId())
                ))
                .toList();
    }

    @Transactional
    public BoardPostDetailResponse getPostDetail(Long postId, Long currentUserId) {
        getPostOrThrow(postId);
        boardPostRepository.incrementViewCount(postId);
        BoardPost post = getPostOrThrow(postId);
        boolean liked = currentUserId != null && boardPostLikeRepository.existsByUserIdAndPostId(currentUserId, postId);
        long likeCount = boardPostLikeRepository.countByPostId(postId);
        List<BoardPostImageResponse> images = boardPostImageRepository.findByPostIdOrderByCreatedAtAsc(postId)
                .stream()
                .map(i -> new BoardPostImageResponse(i.getId(), i.getImageUrl())).toList();
        return new BoardPostDetailResponse(post, currentUserId, liked, likeCount, images);
    }

    @Transactional
    public BoardPostCreateResponse createPost(Long userId, BoardPostCreateRequest request) {
        User author = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        BoardPost post = BoardPost.builder()
                .author(author)
                .category(request.getCategory())
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

        post.update(request.getCategory(), request.getTitle(), request.getContent());
        log.info("게시글 수정: postId={}, userId={}", postId, userId);

        boolean liked = boardPostLikeRepository.existsByUserIdAndPostId(userId, postId);
        long likeCount = boardPostLikeRepository.countByPostId(postId);
        List<BoardPostImageResponse> images = boardPostImageRepository.findByPostIdOrderByCreatedAtAsc(postId)
                .stream()
                .map(i -> new
                        BoardPostImageResponse(i.getId(), i.getImageUrl()))
                .toList();
        return new BoardPostDetailResponse(post, userId, liked, likeCount, images);
    }

    @Transactional
    public void deletePost(Long postId, Long userId) {
        BoardPost post = getPostOrThrow(postId);
        validateAuthor(post, userId);

        List<BoardPostImage> images =
                boardPostImageRepository.findByPostId(postId);
        for (BoardPostImage image : images) {
            imageStorageService.delete(image.getImageUrl());
        }

        List<Long> commentIds = boardCommentRepository.findIdsByPostId(postId);
        if (!commentIds.isEmpty()) {
            boardCommentLikeRepository.deleteByCommentIdIn(commentIds);
        }
        boardCommentRepository.deleteRepliesByPostId(postId);
        boardCommentRepository.deleteParentsByPostId(postId);
        boardPostLikeRepository.deleteByPostId(postId);
        boardPostImageRepository.deleteAll(images);
        boardPostRepository.delete(post);
        log.info("게시글 삭제: postId={}, userId={}", postId, userId);
    }

    @Transactional
    public void adminDeletePost(Long postId) {
        BoardPost post = getPostOrThrow(postId);

        List<Long> commentIds = boardCommentRepository.findIdsByPostId(postId);
        if (!commentIds.isEmpty()) {
            boardCommentLikeRepository.deleteByCommentIdIn(commentIds);
            boardCommentReportRepository.deleteByCommentIdIn(commentIds);
        }
        boardCommentRepository.deleteRepliesByPostId(postId);
        boardCommentRepository.deleteParentsByPostId(postId);
        boardPostLikeRepository.deleteByPostId(postId);
        boardPostReportRepository.deleteByPostId(postId);
        boardPostRepository.delete(post);
        log.info("관리자 게시글 삭제: postId={}", postId);
    }

    private BoardPost getPostOrThrow(Long postId) {
        return boardPostRepository.findById(postId)
                .orElseThrow(() -> new NoSuchElementException("게시글을 찾을 수 없습니다."));
    }

    private void validateAuthor(BoardPost post, Long userId) {
        if (userId == null || post.getAuthor() == null || !userId.equals(post.getAuthor().getId())) {
            log.warn("게시글 수정/삭제 권한 없음: postId={}, userId={}", post.getId(), userId);
            throw new AccessDeniedException("게시글 수정/삭제 권한이 없습니다.");
        }
    }

    @Transactional
    public List<BoardPostImageResponse> uploadImages(Long
                                                             postId, Long userId, List<MultipartFile> files) {
        BoardPost post = getPostOrThrow(postId);
        validateAuthor(post, userId);

        int currentCount =
                boardPostImageRepository.countByPostId(postId);
        if (currentCount + files.size() > 5) {
            throw new IllegalArgumentException("이미지는 최대 5장까지 업로드 가능합니다.");
        }

        List<BoardPostImageResponse> responses = new ArrayList<>();
        for (MultipartFile file : files) {
            String imageUrl = imageStorageService.store(file);
            BoardPostImage image =
                    BoardPostImage.builder()
                            .post(post)
                            .imageUrl(imageUrl)
                            .build();
            BoardPostImage saved = boardPostImageRepository.save(image);
            responses.add(new BoardPostImageResponse(saved.getId(), saved.getImageUrl()));
        }
        return responses;
    }

    @Transactional
    public void deleteImage(Long postId, Long imageId, Long userId) {
        BoardPost post = getPostOrThrow(postId);
        validateAuthor(post, userId);

        BoardPostImage image = boardPostImageRepository.findByIdAndPostId(imageId, postId)
                        .orElseThrow(() -> new NoSuchElementException("이미지를 찾을 수 없습니다."));
        imageStorageService.delete(image.getImageUrl());
        boardPostImageRepository.delete(image);
    }

}
