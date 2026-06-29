package com.example.projectback.board.repository;

import com.example.projectback.entity.BoardCommentReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface BoardCommentReportRepository extends JpaRepository<BoardCommentReport, Long> {

    boolean existsByUserIdAndCommentId(Long userId, Long commentId);

    void deleteByUserId(Long userId);

    void deleteByCommentIdIn(Collection<Long> commentIds);

    @Query("SELECT COUNT(r) FROM BoardCommentReport r WHERE r.comment.author.id = :userId")
    long countByCommentAuthorId(@Param("userId") Long userId);

    @Query("SELECT DISTINCT r.comment.author.id FROM BoardCommentReport r")
    List<Long> findDistinctReportedAuthorIds();
}