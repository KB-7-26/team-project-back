package com.example.projectback.board.repository;

import com.example.projectback.entity.BoardPost;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BoardPostRepository extends JpaRepository<BoardPost, Long> {

    Optional<BoardPost> findFirstByIsPinnedTrue();

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
            WHERE (:category IS NULL OR p.category = :category)
            """)
    Page<BoardPost> findByOptionalCategory(@Param("category") String category, Pageable pageable);

    @Query("""
            SELECT p FROM BoardPost p
            WHERE (:category IS NULL OR p.category = :category)
              AND LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
            """)
    Page<BoardPost> findByOptionalCategoryAndTitle(
            @Param("category") String category, @Param("keyword") String keyword, Pageable pageable);

    @Query("""
            SELECT p FROM BoardPost p
            WHERE (:category IS NULL OR p.category = :category)
              AND (LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR LOWER(p.content) LIKE LOWER(CONCAT('%', :keyword, '%')))
            """)
    Page<BoardPost> findByOptionalCategoryAndTitleOrContent(
            @Param("category") String category, @Param("keyword") String keyword, Pageable pageable);

    @Query("""
            SELECT p FROM BoardPost p
            ORDER BY (SELECT COUNT(l) FROM BoardPostLike l WHERE l.post.id = p.id) DESC, p.createdAt DESC
            """)
    List<BoardPost> findTopByLikeCount(Pageable pageable);

    @Query("""
            SELECT p FROM BoardPost p
            WHERE p.createdAt >= :since
            ORDER BY (
                p.viewCount * 1
                + (SELECT COUNT(l) FROM BoardPostLike l WHERE l.post.id = p.id) * 5
                + (SELECT COUNT(c) FROM BoardComment c WHERE c.post.id = p.id) * 10
            ) DESC, p.createdAt DESC
            """)
    List<BoardPost> findTopByScore(@Param("since") LocalDateTime since, Pageable pageable);

    List<BoardPost> findAllByOrderByViewCountDescCreatedAtDesc(Pageable pageable);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE BoardPost p SET p.viewCount = p.viewCount + 1 WHERE p.id = :id")
    void incrementViewCount(@Param("id") Long id);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE BoardPost p SET p.isPinned = false WHERE p.isPinned = true")
    void unpinAll();

    @Query("SELECT p.id FROM BoardPost p WHERE p.author.id = :authorId")
    List<Long> findIdsByAuthorId(@Param("authorId") Long authorId);
}
