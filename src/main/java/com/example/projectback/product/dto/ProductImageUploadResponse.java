package com.example.projectback.product.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ProductImageUploadResponse {
    private Long id;
    private String imageUrl;
    private Integer sortOrder;
}
