package com.example.projectback.product.repository;

import com.example.projectback.entity.Product;
import com.example.projectback.product.dto.ProductListResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductRepository extends JpaRepository<Product, Long> {

    @Query(value = """
            SELECT new com.example.projectback.product.dto.ProductListResponse(
                p.id, p.title, p.price, p.isFree, p.saleStatus, p.location,
                (SELECT pi.imageUrl FROM ProductImage pi WHERE pi.product = p ORDER BY pi.sortOrder ASC LIMIT 1),
                p.seller.nickname,
                (SELECT COUNT(pf) FROM ProductFavorite pf WHERE pf.product = p),
                p.createdAt
            )
            FROM Product p
            WHERE (:categoryId IS NULL OR p.category.id = :categoryId)
              AND (:saleStatus IS NULL OR :saleStatus = 'all' OR p.saleStatus = :saleStatus)
            """,
            countQuery = """
            SELECT COUNT(p) FROM Product p
            WHERE (:categoryId IS NULL OR p.category.id = :categoryId)
              AND (:saleStatus IS NULL OR :saleStatus = 'all' OR p.saleStatus = :saleStatus)
            """)
    Page<ProductListResponse> findProductList(@Param("categoryId") Long categoryId,
                                              @Param("saleStatus") String saleStatus,
                                              Pageable pageable);
}