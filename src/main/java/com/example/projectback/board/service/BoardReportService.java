package com.example.projectback.board.service;

import com.example.projectback.board.repository.BoardCommentReportRepository;
import com.example.projectback.board.repository.BoardCommentRepository;
import com.example.projectback.board.repository.BoardPostReportRepository;
import com.example.projectback.board.repository.BoardPostRepository;
import com.example.projectback.common.exception.DuplicateResourceException;
import com.example.projectback.entity.BoardComment;
import com.example.projectback.entity.BoardCommentReport;
import com.example.projectback.entity.BoardPost;
import com.example.projectback.entity.BoardPostReport;
import com.example.projectback.entity.User;
import com.example.projectback.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;

@Slf4j
@Service
@RequiredArgsConstructor
public class BoardReportService {

    private final BoardPostRepository boardPostRepository;
    private final BoardCommentRepository boardCommentRepository;
    private final BoardPostReportRepository boardPostReportRepository;
    private final BoardCommentReportRepository boardCommentReportRepository;
    private final UserRepository userRepository;

    @Transactional
    public void reportPost(Long postId, Long userId, String reason) {
        if (boardPostReportRepository.existsByUserIdAndPostId(userId, postId)) {
            throw new DuplicateResourceException("이미 신고한 게시글입니다.");
        }

        BoardPost post = boardPostRepository.findById(postId)
                .orElseThrow(() -> new NoSuchElementException("게시글을 찾을 수 없습니다."));
        User user = getUserOrThrow(userId);

        boardPostReportRepository.save(
                BoardPostReport.builder().user(user).post(post).reason(reason).build()
        );
        log.info("게시글 신고: postId={}, userId={}, reason={}", postId, userId, reason);
    }

    @Transactional
    public void reportComment(Long postId, Long commentId, Long userId, String reason) {
        BoardComment comment = boardCommentRepository.findById(commentId)
                .orElseThrow(() -> new NoSuchElementException("댓글을 찾을 수 없습니다."));
        if (!comment.getPost().getId().equals(postId)) {
            throw new IllegalArgumentException("해당 게시글의 댓글이 아닙니다.");
        }

        if (boardCommentReportRepository.existsByUserIdAndCommentId(userId, commentId)) {
            throw new DuplicateResourceException("이미 신고한 댓글입니다.");
        }

        User user = getUserOrThrow(userId);

        boardCommentReportRepository.save(
                BoardCommentReport.builder().user(user).comment(comment).reason(reason).build()
        );
        log.info("댓글 신고: postId={}, commentId={}, userId={}, reason={}", postId, commentId, userId, reason);
    }

    private User getUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));
    }
}