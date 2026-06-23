package com.example.projectback.board.repository;

import com.example.projectback.entity.BoardPostReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BoardPostReportRepository extends JpaRepository<BoardPostReport, Long> {

    boolean existsByUserIdAndPostId(Long userId, Long postId);

    void deleteByPostId(Long postId);

    void deleteByUserId(Long userId);

    @Query("SELECT COUNT(r) FROM BoardPostReport r WHERE r.post.author.id = :userId")
    long countByPostAuthorId(@Param("userId") Long userId);

    @Query("SELECT DISTINCT r.post.author.id FROM BoardPostReport r")
    List<Long> findDistinctReportedAuthorIds();
}