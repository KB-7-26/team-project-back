package com.example.projectback.board.repository;

import com.example.projectback.entity.BoardCommentReport;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BoardCommentReportRepository extends JpaRepository<BoardCommentReport, Long> {

    boolean existsByUserIdAndCommentId(Long userId, Long commentId);

    void deleteByUserId(Long userId);

    void deleteByCommentIdIn(java.util.Collection<Long> commentIds);
}