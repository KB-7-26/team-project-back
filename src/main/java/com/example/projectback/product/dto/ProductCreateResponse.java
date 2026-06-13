package com.example.projectback.product.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ProductCreateResponse {
    private Long id;
    private String title;
    private Integer price;
    private Boolean isFree;
    private String saleStatus;
    private LocalDateTime createdAt;
}
