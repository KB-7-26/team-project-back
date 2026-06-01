package com.example.projectback.product.service;

import com.example.projectback.entity.Category;
import com.example.projectback.entity.Product;
import com.example.projectback.entity.ProductImage;
import com.example.projectback.entity.User;
import com.example.projectback.image.service.ImageStorageService;
import com.example.projectback.product.dto.ProductCreateRequest;
import com.example.projectback.product.dto.ProductCreateResponse;
import com.example.projectback.product.dto.ProductDetailResponse;
import com.example.projectback.product.dto.ProductImageUploadResponse;
import com.example.projectback.product.dto.ProductListResponse;
import com.example.projectback.product.repository.CategoryRepository;
import com.example.projectback.product.repository.ProductFavoriteRepository;
import com.example.projectback.product.repository.ProductImageRepository;
import com.example.projectback.product.repository.ProductRepository;
import com.example.projectback.security.CurrentUserProvider;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;
    private final ProductImageRepository productImageRepository;
    private final ProductFavoriteRepository productFavoriteRepository;
    private final CategoryRepository categoryRepository;
    private final CurrentUserProvider currentUserProvider;
    private final ImageStorageService imageStorageService;

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

        return new ProductCreateResponse(
                product.getId(),
                product.getTitle(),
                product.getPrice(),
                product.getIsFree(),
                product.getSaleStatus(),
                product.getCreatedAt()
        );
    }

    @Transactional
    public ProductDetailResponse getProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("상품을 찾을 수 없습니다."));

        product.incrementViewCount();

        List<String> imageUrls = productImageRepository.findByProductIdOrderBySortOrderAsc(id)
                .stream()
                .map(ProductImage::getImageUrl)
                .toList();

        long favoriteCount = productFavoriteRepository.countByProductId(id);

        return new ProductDetailResponse(
                product.getId(),
                product.getTitle(),
                product.getDescription(),
                product.getPrice(),
                product.getIsFree(),
                product.getProductCondition(),
                product.getSaleStatus(),
                product.getLocation(),
                product.getViewCount(),
                product.getCategory().getName(),
                product.getSeller().getId(),
                product.getSeller().getNickname(),
                product.getSeller().getProfileImageUrl(),
                imageUrls,
                favoriteCount,
                product.getCreatedAt()
        );
    }

    @Transactional(readOnly = true)
    public Page<ProductListResponse> getProducts(Long categoryId,
                                                 String saleStatus, Pageable pageable) {
        return productRepository.findProductList(categoryId, saleStatus, pageable);
    }

    // 이미지 파일들을 받아서 저장하고 DB에 기록
    @Transactional
    public List<ProductImageUploadResponse> uploadImages(Long productId, List<MultipartFile> files){
        Product product = productRepository.findById(productId).orElseThrow(() -> new EntityNotFoundException("상품을 찾을 수 없습니다."));
        int currentCount = productImageRepository.countByProductId(productId);
        List<ProductImageUploadResponse> responses = new ArrayList<>();

        for(int i = 0; i < files.size(); i++){
            String imageUrl = imageStorageService.store(files.get(i));
            ProductImage image = ProductImage.builder().product(product).imageUrl(imageUrl).sortOrder(currentCount + i).build();
            ProductImage saved = productImageRepository.save(image);
            responses.add(new ProductImageUploadResponse(saved.getId(), saved.getImageUrl(), saved.getSortOrder()));
        }
        return responses;
    }

    // 이미지 ID 받아서 파일이랑 DB 둘 다 삭제
    @Transactional
    public void deleteImage(Long productId, Long imageId){
        ProductImage image = productImageRepository.findByIdAndProductId(imageId,productId)
                .orElseThrow(() -> new EntityNotFoundException("이미지를 찾을 수 없습니다."));

        imageStorageService.delete(image.getImageUrl());
        productImageRepository.delete(image);
    }



}

