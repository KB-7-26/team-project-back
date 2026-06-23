package com.example.projectback.board.repository;

import com.example.projectback.entity.BoardPostReport;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BoardPostReportRepository extends JpaRepository<BoardPostReport, Long> {

    boolean existsByUserIdAndPostId(Long userId, Long postId);

    void deleteByPostId(Long postId);

    void deleteByUserId(Long userId);
}