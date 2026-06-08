package com.example.projectback.board.service;

import com.example.projectback.board.dto.LikeResponse;
import com.example.projectback.board.repository.BoardCommentLikeRepository;
import com.example.projectback.board.repository.BoardCommentRepository;
import com.example.projectback.board.repository.BoardPostLikeRepository;
import com.example.projectback.board.repository.BoardPostRepository;
import com.example.projectback.entity.BoardComment;
import com.example.projectback.entity.BoardCommentLike;
import com.example.projectback.entity.BoardPost;
import com.example.projectback.entity.BoardPostLike;
import com.example.projectback.entity.User;
import com.example.projectback.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;
import java.util.function.BooleanSupplier;
import java.util.function.LongSupplier;

@Slf4j
@Service
@RequiredArgsConstructor
public class BoardLikeService {

    private final BoardPostLikeRepository boardPostLikeRepository;
    private final BoardCommentLikeRepository boardCommentLikeRepository;
    private final BoardPostRepository boardPostRepository;
    private final BoardCommentRepository boardCommentRepository;
    private final UserRepository userRepository;

    @Transactional
    public LikeResponse togglePostLike(Long postId, Long userId) {
        BoardPost post = boardPostRepository.findById(postId)
                .orElseThrow(() -> new NoSuchElementException("게시글을 찾을 수 없습니다."));
        User user = getUserOrThrow(userId);

        boolean liked = toggleLike(
                () -> boardPostLikeRepository.existsByUserIdAndPostId(userId, postId),
                () -> boardPostLikeRepository.deleteByUserIdAndPostId(userId, postId),
                () -> boardPostLikeRepository.save(BoardPostLike.builder().user(user).post(post).build())
        );

        log.info("게시글 좋아요 {}: postId={}, userId={}", liked ? "추가" : "취소", postId, userId);
        return buildLikeResponse(liked, () -> boardPostLikeRepository.countByPostId(postId));
    }

    @Transactional
    public LikeResponse toggleCommentLike(Long postId, Long commentId, Long userId) {
        BoardComment comment = boardCommentRepository.findById(commentId)
                .orElseThrow(() -> new NoSuchElementException("댓글을 찾을 수 없습니다."));
        if (!comment.getPost().getId().equals(postId)) {
            throw new IllegalArgumentException("해당 게시글의 댓글이 아닙니다.");
        }
        User user = getUserOrThrow(userId);

        boolean liked = toggleLike(
                () -> boardCommentLikeRepository.existsByUserIdAndCommentId(userId, commentId),
                () -> boardCommentLikeRepository.deleteByUserIdAndCommentId(userId, commentId),
                () -> boardCommentLikeRepository.save(BoardCommentLike.builder().user(user).comment(comment).build())
        );

        log.info("댓글 좋아요 {}: postId={}, commentId={}, userId={}", liked ? "추가" : "취소", postId, commentId, userId);
        return buildLikeResponse(liked, () -> boardCommentLikeRepository.countByCommentId(commentId));
    }

    // 좋아요 토글 공통 로직: 이미 있으면 삭제(취소), 없으면 저장(추가)
    private boolean toggleLike(BooleanSupplier existsCheck, Runnable deleteAction, Runnable saveAction) {
        if (existsCheck.getAsBoolean()) {
            deleteAction.run();
            return false;
        }
        saveAction.run();
        return true;
    }

    private LikeResponse buildLikeResponse(boolean liked, LongSupplier countQuery) {
        return new LikeResponse(liked, countQuery.getAsLong());
    }

    private User getUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));
    }
}