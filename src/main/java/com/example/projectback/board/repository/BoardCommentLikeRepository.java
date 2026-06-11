package com.example.projectback.board.repository;

import com.example.projectback.entity.BoardCommentLike;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BoardCommentLikeRepository extends JpaRepository<BoardCommentLike, Long> {

    boolean existsByUserIdAndCommentId(Long userId, Long commentId);

    void deleteByUserIdAndCommentId(Long userId, Long commentId);

    long countByCommentId(Long commentId);

    void deleteByCommentIdIn(java.util.Collection<Long> commentIds);
}