package com.example.projectback.product.repository;

import com.example.projectback.entity.ProductFavorite;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductFavoriteRepository extends JpaRepository<ProductFavorite, Long> {
    long countByProductId(Long productId);

}
