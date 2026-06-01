package com.example.projectback.product.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
public class ProductDetailResponse {
    private Long id;
    private String title;
    private String description;
    private Integer price;
    private Boolean isFree;
    private String productCondition;
    private String saleStatus;
    private String location;
    private Integer viewCount;
    private String categoryName;
    private Long sellerId;
    private String sellerNickname;
    private String sellerProfileImageUrl;
    private List<String> imageUrls;
    private Long favoriteCount;
    private LocalDateTime createdAt;
}