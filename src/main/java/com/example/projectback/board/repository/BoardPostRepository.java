package com.example.projectback.board.repository;

import com.example.projectback.entity.BoardPost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BoardPostRepository extends JpaRepository<BoardPost, Long> {

    @Modifying(clearAutomatically = true)
    @Query("UPDATE BoardPost p SET p.viewCount = p.viewCount + 1 WHERE p.id = :id")
    void incrementViewCount(@Param("id") Long id);
}