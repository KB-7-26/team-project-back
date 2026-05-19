package com.example.projectback.product.repository;

import com.example.projectback.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product,Long> {
    Page<Product> findAllBySaleStatus(String saleStatus, Pageable pageable);
    Page<Product> findAllByCategoryIdAndSaleStatus(Long categoryId, String saleStatus, Pageable pageable);
}
