package com.example.projectback.product.service;

import com.example.projectback.entity.Category;
import com.example.projectback.entity.Product;
import com.example.projectback.entity.ProductImage;
import com.example.projectback.entity.User;
import com.example.projectback.product.dto.ProductCreateRequest;
import com.example.projectback.product.dto.ProductCreateResponse;
import com.example.projectback.product.dto.ProductListResponse;
import com.example.projectback.product.repository.CategoryRepository;
import com.example.projectback.product.repository.ProductImageRepository;
import com.example.projectback.product.repository.ProductRepository;
import com.example.projectback.security.CurrentUserProvider;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;
    private final ProductImageRepository productImageRepository;
    private final CategoryRepository categoryRepository;
    private final CurrentUserProvider currentUserProvider;

    @Transactional
    public ProductCreateResponse createProduct(ProductCreateRequest request) {
        User seller = currentUserProvider.getCurrentUser();

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new EntityNotFoundException("카테고리를 찾을 수 없습니다."));

        Product product = Product.builder()
                .seller(seller)
                .category(category)
                .title(request.getTitle())
                .description(request.getDescription())
                .price(request.getPrice())
                .isFree(request.getIsFree())
                .productCondition(request.getProductCondition())
                .location(request.getLocation())
                .build();

        productRepository.save(product);

        for (String imageUrl : request.getImageUrls()) {
            ProductImage image = ProductImage.builder()
                    .product(product)
                    .imageUrl(imageUrl)
                    .build();
            productImageRepository.save(image);
        }

        return new ProductCreateResponse(
                product.getId(),
                product.getTitle(),
                product.getPrice(),
                product.getIsFree(),
                product.getSaleStatus(),
                product.getCreatedAt()
        );
    }

    @Transactional(readOnly = true)
    public Page<ProductListResponse> getProducts(Long categoryId,
                                                 String saleStatus, Pageable pageable) {
        return productRepository.findProductList(categoryId, saleStatus, pageable);
    }

}

