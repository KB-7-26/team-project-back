package com.example.projectback.product.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ProductCreateRequest {
    private Long sellerId;
    private Long categoryId;
    private String title;
    private String description;
    private Integer price;
    private Boolean isFree;
    private String productCondition;
    private String location;
    private List<String> imageUrls;
}
