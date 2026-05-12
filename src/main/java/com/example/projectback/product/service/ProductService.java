package com.example.projectback.product.service;

import com.example.projectback.entity.Product;
import com.example.projectback.entity.ProductImage;
import com.example.projectback.product.dto.ProductCreateRequest;
import com.example.projectback.product.dto.ProductCreateResponse;
import com.example.projectback.product.repository.ProductImageRepository;
import com.example.projectback.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;
    private final ProductImageRepository productImageRepository;

    public ProductCreateResponse createResponse(ProductCreateRequest request) {
        Product product = Product.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .price(request.getPrice())
                .isFree(request.getIsFree())
                .productCondition(request.getProductCondition())
                .location(request.getLocation())
                .build();

        productRepository.save(product);

        for(String imageUrl : request.getImageUrls()){
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

}

