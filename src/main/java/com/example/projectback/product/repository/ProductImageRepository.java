package com.example.projectback.product.repository;

import com.example.projectback.entity.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProductImageRepository extends JpaRepository<ProductImage,Long> {
    Optional<ProductImage> findFirstByProductIdOrderBySortOrderAsc(Long productId);

}

