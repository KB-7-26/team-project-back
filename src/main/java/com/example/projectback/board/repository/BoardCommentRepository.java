package com.example.projectback.board.repository;

import com.example.projectback.entity.BoardComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BoardCommentRepository extends JpaRepository<BoardComment, Long> {
    List<BoardComment> findByPostIdOrderByCreatedAtAsc(Long postId);

    long countByPostId(Long postId);

    long countByAuthorId(Long authorId);

    @Query("SELECT c.id FROM BoardComment c WHERE c.post.id = :postId")
    List<Long> findIdsByPostId(@Param("postId") Long postId);

    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM BoardComment c WHERE c.post.id = :postId AND c.parentComment IS NOT NULL")
    void deleteRepliesByPostId(@Param("postId") Long postId);

    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM BoardComment c WHERE c.post.id = :postId AND c.parentComment IS NULL")
    void deleteParentsByPostId(@Param("postId") Long postId);
}
