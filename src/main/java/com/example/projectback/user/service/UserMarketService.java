package com.example.projectback.user.service;

import com.example.projectback.entity.User;
import com.example.projectback.product.dto.ProductListResponse;
import com.example.projectback.product.repository.ProductFavoriteRepository;
import com.example.projectback.product.repository.ProductRepository;
import com.example.projectback.security.CurrentUserProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserMarketService {
    private final ProductRepository productRepository;
    private final ProductFavoriteRepository productFavoriteRepository;
    private final CurrentUserProvider currentUserProvider;

    @Transactional(readOnly = true)
    public Page<ProductListResponse> getMyProducts(String saleStatus, Pageable pageable) {
        User user = currentUserProvider.getCurrentUser();
        return productRepository.findMyProducts(user.getId(), saleStatus, pageable);
    }

    @Transactional(readOnly = true)
    public List<ProductListResponse> getMyFavorites() {
        User user = currentUserProvider.getCurrentUser();
        return productFavoriteRepository.findFavoriteProductsByUserId(user.getId());
    }
}
