package com.example.projectback.product.repository;

import com.example.projectback.entity.ProductFavorite;
import com.example.projectback.product.dto.ProductListResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductFavoriteRepository extends JpaRepository<ProductFavorite, Long> {

    long countByProductId(Long productId);

    long countByUserId(Long userId);

    boolean existsByUserIdAndProductId(Long userId, Long productId);

    void deleteByUserIdAndProductId(Long userId, Long productId);

    void deleteByProductId(Long productId);

    void deleteByUserId(Long userId);

    @Query("""
            SELECT new com.example.projectback.product.dto.ProductListResponse(
                p.id, p.title, p.price, p.isFree, p.saleStatus, p.location,
                (SELECT pi.imageUrl FROM ProductImage pi WHERE pi.product = p ORDER BY pi.sortOrder ASC LIMIT 1),
                p.seller.nickname,
                (SELECT COUNT(pf2) FROM ProductFavorite pf2 WHERE pf2.product = p),
                p.viewCount,
                p.createdAt
            )
            FROM ProductFavorite pf
            JOIN pf.product p
            WHERE pf.user.id = :userId
            ORDER BY pf.createdAt DESC
            """)
    List<ProductListResponse> findFavoriteProductsByUserId(@Param("userId") Long userId);
}
