package com.example.projectback.board.repository;

import com.example.projectback.entity.BoardPostImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BoardPostImageRepository extends JpaRepository<BoardPostImage, Long> {
    List<BoardPostImage> findByPostIdOrderByCreatedAtAsc(Long postId);
    Optional<BoardPostImage> findByIdAndPostId(Long id, Long postId);
    int countByPostId(Long postId);
    List<BoardPostImage> findByPostId(Long postId);
}
