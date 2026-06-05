package com.example.projectback.transaction.repository;

import com.example.projectback.entity.Transaction;
import com.example.projectback.product.dto.ProductListResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    @Query(value = """
            SELECT new com.example.projectback.product.dto.ProductListResponse(
                p.id, p.title, p.price, p.isFree, p.saleStatus, p.location,
                (SELECT pi.imageUrl FROM ProductImage pi WHERE pi.product = p ORDER BY pi.sortOrder ASC LIMIT 1),
                p.seller.nickname,
                (SELECT COUNT(pf) FROM ProductFavorite pf WHERE pf.product = p),
                p.viewCount,
                p.createdAt
            )
            FROM Transaction t
            JOIN t.product p
            WHERE t.buyer.id = :buyerId
              AND t.status = 'completed'
            """,
            countQuery = """
            SELECT COUNT(t)
            FROM Transaction t
            WHERE t.buyer.id = :buyerId
              AND t.status = 'completed'
            """)
    Page<ProductListResponse> findCompletedPurchaseProductsByBuyerId(@Param("buyerId") Long buyerId,
                                                                     Pageable pageable);
}
