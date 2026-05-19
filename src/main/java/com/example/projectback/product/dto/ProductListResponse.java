package com.example.projectback.product.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class ProductListResponse {
    private Long id;
    private String title;
    private Integer price;
    private Boolean isFree;
    private String saleStatus;
    private String location;
    private String thumbnailUrl;
    private String sellerNickname;
    private Long favoriteCount;
    private LocalDateTime createdAt;
}
