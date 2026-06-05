package com.example.projectback.product.service;

import com.example.projectback.entity.Product;
import com.example.projectback.entity.ProductFavorite;
import com.example.projectback.entity.User;
import com.example.projectback.product.repository.ProductFavoriteRepository;
import com.example.projectback.product.repository.ProductRepository;
import com.example.projectback.security.CurrentUserProvider;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProductFavoriteService {
    private final ProductFavoriteRepository productFavoriteRepository;
    private final ProductRepository productRepository;
    private final CurrentUserProvider currentUserProvider;

    @Transactional
    public void toggleFavorite(Long productId) {
        User user = currentUserProvider.getCurrentUser();
        if (productFavoriteRepository.existsByUserIdAndProductId(user.getId(), productId)) {
            productFavoriteRepository.deleteByUserIdAndProductId(user.getId(), productId);
        } else {
            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new EntityNotFoundException("상품을 찾을 수 없습니다."));
            productFavoriteRepository.save(new ProductFavorite(user, product));
        }
    }
}
