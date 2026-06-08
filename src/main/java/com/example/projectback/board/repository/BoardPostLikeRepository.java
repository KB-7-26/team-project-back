package com.example.projectback.board.repository;

import com.example.projectback.entity.BoardPostLike;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BoardPostLikeRepository extends JpaRepository<BoardPostLike, Long> {

    boolean existsByUserIdAndPostId(Long userId, Long postId);

    void deleteByUserIdAndPostId(Long userId, Long postId);

    long countByPostId(Long postId);
}