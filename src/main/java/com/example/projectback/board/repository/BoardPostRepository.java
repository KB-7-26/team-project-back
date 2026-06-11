package com.example.projectback.board.repository;

import com.example.projectback.entity.BoardPost;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BoardPostRepository extends JpaRepository<BoardPost, Long> {

    Page<BoardPost> findByAuthorId(Long authorId, Pageable pageable);

    long countByAuthorId(Long authorId);

    @Query(
            value = """
                    SELECT p
                    FROM BoardPost p
                    WHERE p.id IN (
                        SELECT DISTINCT c.post.id
                        FROM BoardComment c
                        WHERE c.author.id = :authorId
                    )
                    """,
            countQuery = """
                    SELECT COUNT(p)
                    FROM BoardPost p
                    WHERE p.id IN (
                        SELECT DISTINCT c.post.id
                        FROM BoardComment c
                        WHERE c.author.id = :authorId
                    )
                    """
    )
    Page<BoardPost> findCommentedPostsByAuthorId(@Param("authorId") Long authorId, Pageable pageable);

    @Query("""
            SELECT p FROM BoardPost p
            WHERE LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
            """)
    Page<BoardPost> findByTitleContainingIgnoreCase(@Param("keyword") String keyword, Pageable pageable);

    @Query("""
            SELECT p FROM BoardPost p
            WHERE LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
               OR LOWER(p.content) LIKE LOWER(CONCAT('%', :keyword, '%'))
            """)
    Page<BoardPost> findByTitleOrContentContainingIgnoreCase(@Param("keyword") String keyword, Pageable pageable);

    @Query("""
            SELECT p FROM BoardPost p
            ORDER BY (SELECT COUNT(l) FROM BoardPostLike l WHERE l.post.id = p.id) DESC, p.createdAt DESC
            """)
    List<BoardPost> findTopByLikeCount(Pageable pageable);

    List<BoardPost> findAllByOrderByViewCountDescCreatedAtDesc(Pageable pageable);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE BoardPost p SET p.viewCount = p.viewCount + 1 WHERE p.id = :id")
    void incrementViewCount(@Param("id") Long id);
}
