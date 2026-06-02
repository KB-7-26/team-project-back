package com.example.projectback.product.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ProductUpdateRequest {
    private Long categoryId;
    private String title;
    private String description;
    private Integer price;
    private Boolean isFree;
    private String productCondition;
    private String location;
    private String saleStatus;
}
